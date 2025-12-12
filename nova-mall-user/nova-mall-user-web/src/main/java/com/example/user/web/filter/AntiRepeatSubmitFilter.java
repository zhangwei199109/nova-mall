package com.example.user.web.filter;

import com.example.common.dto.Result;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 简单防重复提交：基于请求指纹+短TTL。
 */
@Component
public class AntiRepeatSubmitFilter extends OncePerRequestFilter {

    private static final String HDR_USER_ID = "X-User-Id";
    private static final String HDR_TRACE_ID = "X-Trace-Id";

    private final Cache<String, Boolean> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(5))
            .maximumSize(10_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 登录等认证接口需要读取请求体，避免重复读取导致下游拿不到 body
        String uri = request.getRequestURI();
        if (uri.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 只针对修改类请求
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
        try {
            String fingerprint = buildFingerprint(wrapped);
            if (cache.getIfPresent(fingerprint) != null) {
                reject(response);
                return;
            }
            cache.put(fingerprint, Boolean.TRUE);
            filterChain.doFilter(wrapped, response);
        } finally {
            // 清理缓存的请求体
            wrapped.getContentAsByteArray();
        }
    }

    private String buildFingerprint(ContentCachingRequestWrapper request) throws IOException {
        String userId = request.getHeader(HDR_USER_ID);
        if (userId == null) {
            userId = request.getRemoteAddr();
        }
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String body = readBody(request);
        String raw = userId + "|" + method + "|" + uri + "|" + body;
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String readBody(ContentCachingRequestWrapper request) throws IOException {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length == 0) {
            buf = request.getInputStream().readAllBytes();
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":429,\"message\":\"请勿重复提交\"}";
        response.getWriter().write(body);
        response.getWriter().flush();
    }
}

