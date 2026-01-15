package com.example.pay.service.channel;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlipayNotifyResult {
    private String payNo;
    private String tradeNo;
    private String tradeStatus;
    private BigDecimal amount;
}










