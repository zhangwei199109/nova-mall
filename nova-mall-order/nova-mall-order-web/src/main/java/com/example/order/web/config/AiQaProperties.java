package com.example.order.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ai.qa")
public class AiQaProperties {

    /**
     * 向量检索服务地址（如 ES/Faiss 网关），可为空则回退 FAQ。
     */
    private String vectorEndpoint;

    /**
     * 大模型 HTTP 接口地址。
     */
    private String llmEndpoint;

    /**
     * 大模型名称（阿里 DashScope 示例：qwen-turbo / qwen-plus）。
     */
    private String llmModel;

    /**
     * 大模型 API Key（若需要）。
     */
    private String llmApiKey;

}

