package com.example.gateway.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TokenBlacklist {

    private final Cache<String, Instant> cache = Caffeine.newBuilder()
            .maximumSize(20_000)
            .build();

    public void blacklist(String jti, Instant expiresAt) {
        cache.put(jti, expiresAt);
    }

    public boolean isBlacklisted(String jti) {
        Instant exp = cache.getIfPresent(jti);
        if (exp == null) {
            return false;
        }
        if (exp.isBefore(Instant.now())) {
            cache.invalidate(jti);
            return false;
        }
        return true;
    }
}








