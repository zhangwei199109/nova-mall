package com.example.user.web.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class RefreshTokenStore {

    private final Cache<String, RefreshSession> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .build();

    public void save(String tokenHash, RefreshSession session) {
        cache.put(tokenHash, session);
    }

    public Optional<RefreshSession> get(String tokenHash) {
        RefreshSession session = cache.getIfPresent(tokenHash);
        if (session == null) {
            return Optional.empty();
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            cache.invalidate(tokenHash);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void remove(String tokenHash) {
        cache.invalidate(tokenHash);
    }

    public record RefreshSession(String userId, String username, java.util.List<String> roles, Instant expiresAt, String deviceId) {}
}

