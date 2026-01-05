package com.example.user.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 从网关透传的 Header 中组装 Authentication，供 @PreAuthorize 使用。
 */
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    private static final String HDR_USER_ID = "X-User-Id";
    private static final String HDR_USER_NAME = "X-User-Name";
    private static final String HDR_USER_ROLES = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication auth = buildAuthFromHeaders(request);
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    private Authentication buildAuthFromHeaders(HttpServletRequest request) {
        String userId = request.getHeader(HDR_USER_ID);
        String username = request.getHeader(HDR_USER_NAME);
        String rolesHeader = request.getHeader(HDR_USER_ROLES);
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(username)) {
            return null;
        }
        Collection<? extends GrantedAuthority> authorities = parseRoles(rolesHeader);
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    private Collection<? extends GrantedAuthority> parseRoles(String rolesHeader) {
        if (StringUtils.isBlank(rolesHeader)) {
            return Collections.emptyList();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .map(r -> {
                    // 统一 ROLE_ 前缀
                    if (!r.startsWith("ROLE_")) {
                        return "ROLE_" + r;
                    }
                    return r;
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}



























