package com.example.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ai.qa")
public class AiQaProperties {
    private String vectorEndpoint;
    private String llmEndpoint;
    private String llmModel;
    private String llmApiKey;

    /** HTTP 连接超时（毫秒），默认 5000 */
    private int connectTimeoutMs = 5000;
    /** HTTP 读超时（毫秒），默认 15000 */
    private int readTimeoutMs = 15000;
    /** 流式请求整体超时（毫秒），默认 30000 */
    private int streamTimeoutMs = 30000;
    /** LLM 请求重试次数（非 2xx 时），默认 0 不重试 */
    private int retry = 0;
}

