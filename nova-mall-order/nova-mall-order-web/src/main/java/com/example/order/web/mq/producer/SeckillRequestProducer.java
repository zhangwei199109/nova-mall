package com.example.order.web.mq.producer;

import com.example.order.web.mq.RocketMqTopics;
import com.example.order.web.mq.message.SeckillRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillRequestProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void send(SeckillRequestMessage payload) {
        log.info("[MQ] send seckill request, reqId={}, user={}, act={}, qty={}",
                payload.getRequestId(), payload.getUserId(), payload.getActivityId(), payload.getQuantity());
        rocketMQTemplate.send(RocketMqTopics.SECKILL_REQUEST_TOPIC,
                MessageBuilder.withPayload(payload).build());
    }
}





