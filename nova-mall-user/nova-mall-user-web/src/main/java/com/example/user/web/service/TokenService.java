package com.example.user.web.service;

import com.example.user.web.service.RefreshTokenStore.RefreshSession;
import com.example.user.service.UserAppService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    private final SecretKey secretKey;
    private final long accessExpireMinutes;
    private final long refreshExpireMinutes;
    private final RefreshTokenStore refreshTokenStore;
    private final UserAppService userAppService;

    public TokenService(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expire-minutes:120}") long accessExpireMinutes,
                        @Value("${jwt.refresh-expire-minutes:4320}") long refreshExpireMinutes,
                        RefreshTokenStore refreshTokenStore,
                        UserAppService userAppService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireMinutes = accessExpireMinutes;
        this.refreshExpireMinutes = refreshExpireMinutes;
        this.refreshTokenStore = refreshTokenStore;
        this.userAppService = userAppService;
    }

    public AuthTokens generateTokens(String userId, String username, List<String> roles, String deviceId, String ip, String userAgent) {
        Instant now = Instant.now();
        Instant accessExp = now.plusSeconds(accessExpireMinutes * 60);
        Instant refreshExp = now.plusSeconds(refreshExpireMinutes * 60);

        String accessToken = Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExp))
                .id(UUID.randomUUID().toString())
                .claims(Map.of(
                        "uid", userId,
                        "name", username,
                        "roles", roles,
                        "type", "access"
                ))
                .signWith(secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExp))
                .id(UUID.randomUUID().toString())
                .claims(Map.of(
                        "uid", userId,
                        "name", username,
                        "roles", roles,
                        "type", "refresh"
                ))
                .signWith(secretKey)
                .compact();

        refreshTokenStore.save(hash(refreshToken), new RefreshSession(userId, username, roles, refreshExp, deviceId));
        userAppService.recordSession(Long.parseLong(userId), deviceId, hash(refreshToken), refreshExp, ip, userAgent);
        return new AuthTokens(userId, username, List.copyOf(roles), accessToken, refreshToken);
    }

    public AuthTokens refresh(String refreshToken) {
        Claims claims = parse(refreshToken);
        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new IllegalArgumentException("refresh token 类型不正确");
        }
        String tokenHash = hash(refreshToken);
        RefreshSession session = refreshTokenStore.get(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("refresh token 无效或已过期"));
        // 单次刷新：移除旧的 refresh token
        refreshTokenStore.remove(tokenHash);
        userAppService.deactivateSessionByHash(tokenHash);
        return generateTokens(session.userId(), session.username(), List.copyOf(session.roles()),
                session.deviceId() == null ? "dev-" + session.userId() : session.deviceId(),
                null, null);
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String hash(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }

    public record AuthTokens(String userId, String username, List<String> roles, String accessToken, String refreshToken) {}
}

