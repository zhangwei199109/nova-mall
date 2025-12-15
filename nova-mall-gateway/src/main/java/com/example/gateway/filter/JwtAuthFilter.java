package com.example.gateway.filter;

import com.example.gateway.config.JwtAuthProperties;
import com.example.gateway.service.JwtTokenService;
import com.example.gateway.service.TokenBlacklist;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/**
 * 简单 JWT 鉴权 + TraceId 透传。
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HDR_TRACE_ID = "X-Trace-Id";
    private static final String HDR_USER_ID = "X-User-Id";
    private static final String HDR_USER_NAME = "X-User-Name";
    private static final String HDR_USER_ROLES = "X-User-Roles";

    private final JwtAuthProperties props;
    private final JwtTokenService tokenService;
    private final TokenBlacklist tokenBlacklist;

    public JwtAuthFilter(JwtAuthProperties props, JwtTokenService tokenService, TokenBlacklist tokenBlacklist) {
        this.props = props;
        this.tokenService = tokenService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String traceId = ensureTraceId(exchange);

        if (!props.isEnabled() || isWhitelisted(path)) {
            log.debug("auth skip for path {} traceId {}", path, traceId);
            // 仍然透传 traceId
            return chain.filter(mutateWithTrace(exchange, traceId));
        }

        String authz = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authz) || !authz.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, traceId, "缺少或非法的 Authorization Bearer Token");
        }

        String token = authz.substring(BEARER_PREFIX.length()).trim();
        Claims claims;
        try {
            claims = tokenService.parse(token);
        } catch (Exception e) {
            return unauthorized(exchange, traceId, "Token 校验失败: " + e.getMessage());
        }

        String userId = claims.get("uid", String.class);
        String username = claims.get("name", String.class);
        Object rolesObj = claims.get("roles");
        String roles = "";
        if (rolesObj instanceof List<?> list) {
            roles = String.join(",", list.stream().map(Object::toString).toList());
        } else if (rolesObj != null) {
            roles = rolesObj.toString();
        }
        String jti = claims.getId();
        if (StringUtils.isNotBlank(jti) && tokenBlacklist.isBlacklisted(jti)) {
            return unauthorized(exchange, traceId, "Token 已失效，请重新登录");
        }

        return chain.filter(
                mutateWithHeaders(exchange, traceId, userId, username, roles)
        );
    }

    @Override
    public int getOrder() {
        // 在限流/熔断等之前执行
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = props.getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        return whitelist.stream().anyMatch(path::startsWith);
    }

    private String ensureTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(HDR_TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

    private ServerWebExchange mutateWithTrace(ServerWebExchange exchange, String traceId) {
        return exchange.mutate()
                .request(builder -> builder.headers(httpHeaders -> httpHeaders.set(HDR_TRACE_ID, traceId)))
                .build();
    }

    private ServerWebExchange mutateWithHeaders(ServerWebExchange exchange, String traceId, String userId, String username, String roles) {
        return exchange.mutate()
                .request(builder -> builder.headers(httpHeaders -> {
                    httpHeaders.set(HDR_TRACE_ID, traceId);
                    if (StringUtils.isNotBlank(userId)) {
                        httpHeaders.set(HDR_USER_ID, userId);
                    }
                    if (StringUtils.isNotBlank(username)) {
                        httpHeaders.set(HDR_USER_NAME, username);
                    }
                    if (StringUtils.isNotBlank(roles)) {
                        httpHeaders.set(HDR_USER_ROLES, roles);
                    }
                }))
                .build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String traceId, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HDR_TRACE_ID, traceId);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"traceId\":\"" + traceId + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}

