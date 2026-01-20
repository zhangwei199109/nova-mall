package com.example.order.web.mq;

/**
 * RocketMQ 主题与消费组常量（订单/秒杀）。
 */
public interface RocketMqTopics {

    String SECKILL_REQUEST_TOPIC = "seckill_request_topic";
    String SECKILL_REQUEST_GROUP = "order_seckill_request_group";
}





