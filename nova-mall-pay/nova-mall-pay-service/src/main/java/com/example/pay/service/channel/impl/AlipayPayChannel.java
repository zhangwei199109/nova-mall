package com.example.pay.service.channel.impl;

import com.example.pay.service.channel.PayChannel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 支付宝渠道骨架，返回模拟预支付/退款数据。
 */
@Component
public class AlipayPayChannel implements PayChannel {

    @Override
    public String channel() {
        return "ALIPAY";
    }

    @Override
    public String prepay(String payNo, BigDecimal amount, String subject) {
        // 模拟二维码链接
        return "{\"qrCode\":\"https://mock.alipay.com/qr/" + payNo + "\"}";
    }

    @Override
    public String refund(String payNo, BigDecimal amount, String reason) {
        return "ALI_REF_" + UUID.randomUUID();
    }
}

