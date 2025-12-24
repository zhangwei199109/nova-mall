package com.example.order.web.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.ServletInputStream;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 简单防重复提交：基于请求指纹+短TTL。
 */
@Component
public class AntiRepeatSubmitFilter extends OncePerRequestFilter {

    private static final String HDR_USER_ID = "X-User-Id";

    private final Cache<String, Boolean> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(5))
            .maximumSize(10_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        CachedBodyRequest wrapped = new CachedBodyRequest(request, body);
        try {
            String fingerprint = buildFingerprint(wrapped, body);
            if (cache.getIfPresent(fingerprint) != null) {
                reject(response);
                return;
            }
            cache.put(fingerprint, Boolean.TRUE);
            filterChain.doFilter(wrapped, response);
        } finally {
            // no-op
        }
    }

    private String buildFingerprint(HttpServletRequest request, byte[] body) {
        String userId = request.getHeader(HDR_USER_ID);
        if (userId == null) {
            userId = request.getRemoteAddr();
        }
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String bodyStr = new String(body, StandardCharsets.UTF_8);
        String raw = userId + "|" + method + "|" + uri + "|" + bodyStr;
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":429,\"message\":\"请勿重复提交\"}";
        response.getWriter().write(body);
        response.getWriter().flush();
    }

    private static class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(jakarta.servlet.ReadListener readListener) {
                    // not used
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}

