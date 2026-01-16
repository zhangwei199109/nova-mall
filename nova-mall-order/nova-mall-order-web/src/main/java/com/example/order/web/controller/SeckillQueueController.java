package com.example.order.web.controller;

import com.example.common.dto.Result;
import com.example.common.web.AuthContext;
import com.example.order.api.dto.SeckillPlaceRequest;
import com.example.order.web.mq.message.SeckillRequestMessage;
import com.example.order.web.mq.producer.SeckillRequestProducer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillQueueController {

    private final SeckillRequestProducer seckillRequestProducer;
    private final AuthContext authContext;

    @PostMapping("/request")
    public Result<Long> enqueue(@Valid @RequestBody SeckillPlaceRequest req, HttpServletRequest servletRequest) {
        Long userId = authContext.currentUserId();
        SeckillRequestMessage msg = new SeckillRequestMessage();
        msg.setRequestId(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
        msg.setIdemKey(servletRequest.getHeader("Idempotency-Key"));
        msg.setActivityId(req.getActivityId());
        msg.setQuantity(req.getQuantity());
        msg.setUserId(userId);
        msg.setIp(servletRequest.getRemoteAddr());
        msg.setUserAgent(servletRequest.getHeader("User-Agent"));
        seckillRequestProducer.send(msg);
        return Result.success(msg.getRequestId());
    }
}



