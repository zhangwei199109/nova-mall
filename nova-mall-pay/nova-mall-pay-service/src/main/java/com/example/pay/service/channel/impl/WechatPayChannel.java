package com.example.pay.service.channel.impl;

import com.example.pay.service.channel.PayChannel;
import com.example.pay.service.config.PayChannelProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 微信支付渠道骨架（JSAPI/H5/Native），示例返回预付参数占位。
 */
@Component
public class WechatPayChannel implements PayChannel {

    private final PayChannelProperties properties;

    public WechatPayChannel(PayChannelProperties properties) {
        this.properties = properties;
    }

    @Override
    public String channel() {
        return "WECHAT";
    }

    @Override
    public String prepay(String payNo, BigDecimal amount, String subject) {
        // 示例：返回模拟 prepay 信息；实际应调用微信统一下单接口并生成签名
        String prepayId = "wx_prepay_" + payNo;
        return """
                {"appId":"%s","mchId":"%s","prepayId":"%s","nonceStr":"%s","sign":"%s"}
                """.formatted(
                properties.getWechat().getAppId(),
                properties.getWechat().getMchId(),
                prepayId,
                UUID.randomUUID(),
                "mock-sign");
    }

    @Override
    public String refund(String payNo, BigDecimal amount, String reason) {
        // 示例：返回模拟退款单号；实际应调用微信退款接口
        return "WX_REF_" + payNo;
    }
}

