package com.example.order.web.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpsFeignConfig {

    @Value("${service.ops.internal-token:demo-internal-token}")
    private String internalToken;

    @Bean
    public RequestInterceptor opsInternalTokenInterceptor() {
        return template -> template.header("X-Internal-Token", internalToken);
    }
}
















