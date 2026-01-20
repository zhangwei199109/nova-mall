package com.example.user.web.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static final long EXPIRE_SECONDS = 300; // 5分钟
    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateCode(String key) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(key, new Entry(code, Instant.now().plusSeconds(EXPIRE_SECONDS)));
        return code;
    }

    public boolean verify(String key, String code) {
        Entry entry = store.get(key);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expireAt())) {
            store.remove(key);
            return false;
        }
        boolean ok = entry.code().equals(code);
        if (ok) {
            store.remove(key);
        }
        return ok;
    }

    private record Entry(String code, Instant expireAt) {}
}






