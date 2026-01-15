package com.example.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "退款单")
public class RefundDTO {
    private Long id;
    private String refundNo;
    private String payNo;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String channelRefundNo;
    private String currency;
    private String extra;
    private String status;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

