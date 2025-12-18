package com.example.common.web;

import com.example.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从请求上下文提取用户ID（由网关/认证透传 X-User-Id）。
 */
@Component
public class AuthContext {

    private static final String HDR_USER_ID = "X-User-Id";

    public Long currentUserId() {
        HttpServletRequest request = currentRequest();
        String userIdHeader = request.getHeader(HDR_USER_ID);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new BusinessException(401, "未登录或未透传用户ID");
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(400, "用户ID格式非法");
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attrs).getRequest();
        }
        throw new BusinessException(500, "无法获取请求上下文");
    }
}

