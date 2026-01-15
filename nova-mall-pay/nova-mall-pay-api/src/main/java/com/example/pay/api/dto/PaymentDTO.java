package com.example.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "支付单")
public class PaymentDTO {
    private Long id;
    private String payNo;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String channel;
    private String channelTradeNo;
    private String currency;
    private String extra;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

