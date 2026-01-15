package com.example.order.web.controller;

import com.example.common.dto.Result;
import com.example.common.web.AuthContext;
import com.example.order.api.dto.SeckillPlaceRequest;
import com.example.order.web.mq.message.SeckillRequestMessage;
import com.example.order.web.mq.producer.SeckillRequestProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "秒杀排队", description = "将用户秒杀请求投递到 RocketMQ，由订单侧异步处理")
@RequiredArgsConstructor
public class SeckillQueueController {

    private final SeckillRequestProducer seckillRequestProducer;
    private final AuthContext authContext;

    @PostMapping("/request")
    @Operation(summary = "提交秒杀请求（排队进入 MQ）")
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


