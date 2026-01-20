package com.example.order.web.mq.consumer;

import com.example.common.exception.BusinessException;
import com.example.order.api.dto.SeckillPlaceRequest;
import com.example.order.web.mq.RocketMqTopics;
import com.example.order.web.mq.message.SeckillRequestMessage;
import com.example.order.web.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀排队消费：落在订单侧，复用 SeckillService 下单逻辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = RocketMqTopics.SECKILL_REQUEST_TOPIC,
        consumerGroup = RocketMqTopics.SECKILL_REQUEST_GROUP)
public class SeckillRequestConsumer implements RocketMQListener<SeckillRequestMessage> {

    private final SeckillService seckillService;

    @Override
    public void onMessage(SeckillRequestMessage message) {
        log.info("[MQ] recv seckill request, reqId={}, user={}, act={}, qty={}",
                message.getRequestId(), message.getUserId(), message.getActivityId(), message.getQuantity());
        try {
            SeckillPlaceRequest req = new SeckillPlaceRequest();
            req.setActivityId(message.getActivityId());
            req.setQuantity(message.getQuantity());
            seckillService.place(message.getUserId(), message.getIdemKey(), req);
        } catch (BusinessException ex) {
            log.warn("[MQ] seckill rejected, reqId={}, code={}, msg={}", message.getRequestId(), ex.getCode(), ex.getMessage());
            // 业务异常不重试
        } catch (Exception ex) {
            log.error("[MQ] seckill handle error, reqId={}", message.getRequestId(), ex);
            // 非预期异常交由 MQ 重试
            throw ex;
        }
    }
}





