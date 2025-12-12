package com.example.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关熔断/降级统一返回。
 */
@RestController
public class FallbackController {

    @GetMapping("/fallback/{service}")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> fallback(@PathVariable String service) {
        Map<String, Object> body = new HashMap<>();
        body.put("service", service);
        body.put("message", "服务暂不可用，请稍后重试");
        body.put("timestamp", Instant.now().toString());
        return Mono.just(body);
    }
}



