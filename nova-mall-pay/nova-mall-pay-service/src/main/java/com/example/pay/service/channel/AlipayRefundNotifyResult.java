package com.example.pay.service.channel;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlipayRefundNotifyResult {
    private String refundNo;
    private String channelRefundNo;
    private String payNo;
    private BigDecimal amount;
    private String status;
}











