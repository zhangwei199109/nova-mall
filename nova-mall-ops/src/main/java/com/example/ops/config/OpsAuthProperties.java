package com.example.ops.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ops.auth")
public class OpsAuthProperties {
    /**
     * 运营后台用户名
     */
    private String username = "admin";
    /**
     * 运营后台密码（演示用，生产请改为加密存储）
     */
    private String password = "admin123";
    /**
     * JWT 签名 secret
     */
    private String jwtSecret = "demo-ops-secret";
    /**
     * JWT 过期（分钟）
     */
    private long expireMinutes = 120;
    /**
     * 内部调用 Token（内部服务 Feign 使用）
     */
    private String internalToken = "demo-internal-token";
}

















