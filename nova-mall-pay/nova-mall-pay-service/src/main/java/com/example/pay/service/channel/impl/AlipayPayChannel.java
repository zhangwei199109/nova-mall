package com.example.pay.service.channel.impl;

import com.example.pay.service.channel.PayChannel;
import com.example.pay.service.channel.AlipayChannelClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付宝渠道适配，复用已有 AlipayChannelClient（mock）。
 */
@Component
public class AlipayPayChannel implements PayChannel {

    private final AlipayChannelClient alipayChannelClient;

    public AlipayPayChannel(AlipayChannelClient alipayChannelClient) {
        this.alipayChannelClient = alipayChannelClient;
    }

    @Override
    public String channel() {
        return "ALIPAY";
    }

    @Override
    public String prepay(String payNo, BigDecimal amount, String subject) {
        // 使用预创建返回二维码链接
        return "{\"qrCode\":\"" + alipayChannelClient.precreate(payNo, amount, subject) + "\"}";
    }

    @Override
    public String refund(String payNo, BigDecimal amount, String reason) {
        return alipayChannelClient.refund(payNo, amount, reason);
    }
}

