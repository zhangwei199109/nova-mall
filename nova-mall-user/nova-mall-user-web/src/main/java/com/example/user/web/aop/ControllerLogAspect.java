package com.example.user.web.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class ControllerLogAspect {

    private static final long SLOW_THRESHOLD_MS = 1000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Around("within(com.example.user.web.controller..*)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String handler = sig.getDeclaringType().getSimpleName() + "." + sig.getName();
        Object[] args = joinPoint.getArgs();

        HttpServletRequest req = currentRequest();
        String uri = req != null ? req.getRequestURI() : "";
        String httpMethod = req != null ? req.getMethod() : "";
        String traceId = header(req, "X-Trace-Id");
        String userId = header(req, "X-User-Id");
        String clientIp = req != null ? req.getRemoteAddr() : "";

        StopWatch sw = new StopWatch();
        sw.start();
        try {
            Object result = joinPoint.proceed();
            sw.stop();
            log.info("{}", toJson("user", handler, httpMethod, uri, traceId, userId, clientIp, args, sw.getTotalTimeMillis(), null));
            return result;
        } catch (Throwable t) {
            sw.stop();
            log.error("{}", toJson("user", handler, httpMethod, uri, traceId, userId, clientIp, args, sw.getTotalTimeMillis(), t), t);
            throw t;
        }
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private String header(HttpServletRequest req, String name) {
        return req == null ? "" : req.getHeader(name);
    }

    private String toJson(String service, String handler, String method, String uri,
                          String traceId, String userId, String clientIp, Object[] args,
                          long costMs, Throwable ex) {
        boolean slow = costMs >= SLOW_THRESHOLD_MS;
        try {
            return MAPPER.writeValueAsString(Map.ofEntries(
                    Map.entry("service", service),
                    Map.entry("handler", handler),
                    Map.entry("method", method),
                    Map.entry("uri", uri),
                    Map.entry("traceId", traceId),
                    Map.entry("userId", userId),
                    Map.entry("clientIp", clientIp),
                    Map.entry("args", Arrays.toString(args)),
                    Map.entry("costMs", costMs),
                    Map.entry("slow", slow),
                    Map.entry("error", ex == null ? null : ex.getMessage())
            ));
        } catch (Exception e) {
            return String.format("{service:%s,handler:%s,uri:%s,costMs:%d,slow:%s,error:%s}",
                    service, handler, uri, costMs, slow, ex == null ? null : ex.getMessage());
        }
    }
}

