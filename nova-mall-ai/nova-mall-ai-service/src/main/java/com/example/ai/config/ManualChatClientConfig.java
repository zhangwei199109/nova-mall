package com.example.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当自动配置未生成 ChatClient 时，手动基于 openai 兼容接口创建一个。
 */
@Configuration
public class ManualChatClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ManualChatClientConfig.class);

    @Bean
    @Primary // 优先使用手动创建的 ChatClient，便于控制 baseUrl/model 并输出日志
    public ChatClient manualChatClient(
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model:qwen-turbo}") String model
    ) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("spring.ai.openai.api-key 未配置，无法初始化 ChatClient");
        }
        ClientHttpRequestInterceptor httpLog = (request, body, execution) -> {
            log.info("OpenAiApi HTTP {} {}", request.getMethod(), request.getURI());
            return execution.execute(request, body);
        };
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestInterceptor(httpLog);
        WebClient.Builder webClientBuilder = WebClient.builder().baseUrl(baseUrl);

        log.info("Manual ChatClient using baseUrl={} model={}", baseUrl, model);
        OpenAiApi api = new OpenAiApi(baseUrl, apiKey, restClientBuilder, webClientBuilder);
        OpenAiChatOptions options = OpenAiChatOptions.builder().withModel(model).build();
        OpenAiChatModel chatModel = new OpenAiChatModel(api, options);
        return ChatClient.builder(chatModel).build();
    }
}

