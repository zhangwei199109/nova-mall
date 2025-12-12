package com.example.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * 网关访问日志，JSON 格式，标记慢请求。
 */
@Component
@Slf4j
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final long SLOW_THRESHOLD_MS = 1000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();
        return chain.filter(exchange)
                .doFinally(signal -> {
                    long costMs = (System.nanoTime() - start) / 1_000_000;
                    boolean slow = costMs >= SLOW_THRESHOLD_MS;
                    ServerHttpRequest req = exchange.getRequest();
                    var res = exchange.getResponse();
                    String traceId = header(req, "X-Trace-Id");
                    String userId = header(req, "X-User-Id");
                    String path = req.getURI().getPath();
                    String method = req.getMethod() != null ? req.getMethod().name() : "";
                    String clientIp = clientIp(req);
                    Integer status = res.getStatusCode() != null ? res.getStatusCode().value() : null;
                    log.info("{}", toJson(method, path, status, traceId, userId, clientIp, costMs, slow));
                });
    }

    private String header(ServerHttpRequest req, String name) {
        return req.getHeaders().getFirst(name);
    }

    private String clientIp(ServerHttpRequest req) {
        InetSocketAddress addr = req.getRemoteAddress();
        return addr == null ? "" : addr.getAddress().getHostAddress();
    }

    private String toJson(String method, String path, Integer status, String traceId, String userId,
                          String clientIp, long costMs, boolean slow) {
        try {
            return MAPPER.writeValueAsString(Map.of(
                    "service", "gateway",
                    "method", method,
                    "path", path,
                    "status", status,
                    "traceId", traceId,
                    "userId", userId,
                    "clientIp", clientIp,
                    "costMs", costMs,
                    "slow", slow
            ));
        } catch (Exception e) {
            return String.format("{gateway path:%s status:%s costMs:%d slow:%s}", path, status, costMs, slow);
        }
    }

    @Override
    public int getOrder() {
        // 在较后阶段记录耗时（限流、鉴权等执行后）
        return Ordered.LOWEST_PRECEDENCE;
    }
}

