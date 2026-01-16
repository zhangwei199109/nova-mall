package com.example.pay.service.channel.impl;

import com.example.pay.service.channel.PayChannel;
import com.example.pay.service.config.PayChannelProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 银联渠道骨架（前台跳转/二维码），示例返回 tn/交易号占位。
 */
@Component
public class UnionPayChannel implements PayChannel {

    private final PayChannelProperties properties;

    public UnionPayChannel(PayChannelProperties properties) {
        this.properties = properties;
    }

    @Override
    public String channel() {
        return "UNIONPAY";
    }

    @Override
    public String prepay(String payNo, BigDecimal amount, String subject) {
        // 示例：返回模拟 tn（交易流水号），前端可用此发起支付
        String tn = "UP_TN_" + payNo;
        return """
                {"tn":"%s","frontUrl":"%s","merchantId":"%s"}
                """.formatted(tn, properties.getUnionpay().getFrontUrl(), properties.getUnionpay().getMerchantId());
    }

    @Override
    public String refund(String payNo, BigDecimal amount, String reason) {
        // 示例：返回模拟退款流水
        return "UP_REF_" + UUID.randomUUID();
    }
}

