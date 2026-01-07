package com.example.user.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易内存频控（单节点），生产可替换为 Redis 滑窗。
 */
@Service
public class RateLimiterService {

    private final int perIpMax;
    private final int perAccountMax;
    private final long windowSeconds;

    private final Map<String, Counter> buckets = new ConcurrentHashMap<>();

    public RateLimiterService(@Value("${auth.ratelimit.per-ip:50}") int perIpMax,
                              @Value("${auth.ratelimit.per-account:20}") int perAccountMax,
                              @Value("${auth.ratelimit.window-seconds:300}") long windowSeconds) {
        this.perIpMax = perIpMax;
        this.perAccountMax = perAccountMax;
        this.windowSeconds = windowSeconds;
    }

    public boolean allowIp(String ip) {
        return allow("ip:" + ip, perIpMax);
    }

    public boolean allowAccount(String account) {
        return allow("acct:" + account, perAccountMax);
    }

    private boolean allow(String key, int limit) {
        long now = Instant.now().getEpochSecond();
        Counter counter = buckets.computeIfAbsent(key, k -> new Counter(now, 0));
        synchronized (counter) {
            if (now - counter.windowStart >= windowSeconds) {
                counter.windowStart = now;
                counter.count = 0;
            }
            counter.count += 1;
            return counter.count <= limit;
        }
    }

    private static class Counter {
        long windowStart;
        int count;

        Counter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

