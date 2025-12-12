package com.example.gateway.filter;

import com.example.gateway.config.LocalRateLimitProperties;
import com.example.gateway.config.LocalRateLimitProperties.RouteLimit;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单本地内存限流（令牌桶），按路由前缀限流。
 */
@Component
public class LocalRateLimitFilter implements GlobalFilter, Ordered {

    private final LocalRateLimitProperties properties;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public LocalRateLimitFilter(LocalRateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }
        RouteLimit limit = resolveLimit(exchange.getRequest().getPath().value());
        if (limit == null) {
            return chain.filter(exchange);
        }
        TokenBucket bucket = buckets.computeIfAbsent(limit.getId(), k -> new TokenBucket(limit.getReplenishRate(), limit.getBurstCapacity()));
        if (bucket.tryConsume()) {
            return chain.filter(exchange);
        }
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\",\"timestamp\":\"" + Instant.now() + "\"}";
        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在核心过滤器前尽早拒绝
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private RouteLimit resolveLimit(String path) {
        List<RouteLimit> routes = properties.getRoutes();
        if (!CollectionUtils.isEmpty(routes)) {
            for (RouteLimit route : routes) {
                if (route.getMatchPrefix() != null && path.startsWith(route.getMatchPrefix())) {
                    return route;
                }
            }
        }
        RouteLimit defaults = properties.getDefaults();
        return defaults != null && defaults.getReplenishRate() > 0 ? defaults : null;
    }

    private static class TokenBucket {
        private final long replenishRatePerSec;
        private final long capacity;
        private double tokens;
        private long lastRefillNanos;

        TokenBucket(long replenishRatePerSec, long capacity) {
            this.replenishRatePerSec = Math.max(1, replenishRatePerSec);
            this.capacity = Math.max(1, capacity);
            this.tokens = this.capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            double deltaSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
            if (deltaSeconds <= 0) {
                return;
            }
            double toAdd = deltaSeconds * replenishRatePerSec;
            tokens = Math.min(capacity, tokens + toAdd);
            lastRefillNanos = now;
        }
    }
}

