package com.example.ops.security;

import com.example.ops.config.OpsAuthProperties;
import com.example.ops.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private static final String HDR_INTERNAL = "X-Internal-Token";

    private final OpsAuthProperties authProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 内部 Token 直通
        String internal = request.getHeader(HDR_INTERNAL);
        if (StringUtils.hasText(internal) && internal.equals(authProperties.getInternalToken())) {
            setAuth("internal", List.of("ROLE_OPS_ADMIN"));
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            String token = header.substring(BEARER.length()).trim();
            try {
                Claims claims = (Claims) JwtUtil.parse(token, authProperties.getJwtSecret());
                String sub = claims.getSubject();
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                setAuth(sub, roles);
            } catch (Exception ignored) {
                // 无效 token 继续后续流程，最终由安全框架拒绝
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuth(String username, List<String> roles) {
        Collection<SimpleGrantedAuthority> authorities = roles == null ? List.of() :
                roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}

















