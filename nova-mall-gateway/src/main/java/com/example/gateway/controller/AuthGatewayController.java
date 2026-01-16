package com.example.gateway.controller;

import com.example.gateway.service.JwtTokenService;
import com.example.gateway.service.TokenBlacklist;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthGatewayController {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenService tokenService;
    private final TokenBlacklist tokenBlacklist;

    public AuthGatewayController(JwtTokenService tokenService, TokenBlacklist tokenBlacklist) {
        this.tokenService = tokenService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> logout(@RequestHeader HttpHeaders headers) {
        String traceId = first(headers, "X-Trace-Id", UUID.randomUUID().toString());
        String authz = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authz) || !authz.startsWith(BEARER_PREFIX)) {
            return Mono.just(resp(401, "缺少 Authorization Bearer Token", traceId));
        }
        String token = authz.substring(BEARER_PREFIX.length()).trim();
        Claims claims;
        try {
            claims = tokenService.parse(token);
        } catch (Exception e) {
            return Mono.just(resp(401, "Token 校验失败: " + e.getMessage(), traceId));
        }
        String type = claims.get("type", String.class);
        if (!"access".equals(type)) {
            return Mono.just(resp(400, "仅支持 access token 退出", traceId));
        }
        String jti = claims.getId();
        Instant exp = claims.getExpiration() != null ? claims.getExpiration().toInstant() : Instant.now().plusSeconds(3600);
        if (StringUtils.isNotBlank(jti)) {
            tokenBlacklist.blacklist(jti, exp);
        }
        return Mono.just(resp(200, "success", traceId));
    }

    private Map<String, Object> resp(int code, String msg, String traceId) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", msg);
        map.put("data", null);
        map.put("traceId", traceId);
        return map;
    }

    private String first(HttpHeaders headers, String name, String defaultValue) {
        String val = headers.getFirst(name);
        return StringUtils.isNotBlank(val) ? val : defaultValue;
    }
}































