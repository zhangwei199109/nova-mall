package com.example.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.cloud.gateway.auth")
public class JwtAuthProperties {

    /**
     * 是否启用鉴权。
     */
    private boolean enabled = true;

    /**
     * HMAC 密钥。
     */
    private String secret = "change-me-please-change-me-please-32";

    /**
     * 允许的宽限时间（过期缓冲），默认 30s。
     */
    private Duration leeway = Duration.ofSeconds(30);

    /**
     * 白名单路径前缀，前缀匹配。
     */
    private List<String> whitelist = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getLeeway() {
        return leeway;
    }

    public void setLeeway(Duration leeway) {
        this.leeway = leeway;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }
}

