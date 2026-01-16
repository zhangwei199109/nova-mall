package com.example.order.web.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeckillRequestMessage implements Serializable {
    private Long requestId;
    private String idemKey;
    private Long userId;
    private Long activityId;
    private Integer quantity;
    private String ip;
    private String userAgent;
}



