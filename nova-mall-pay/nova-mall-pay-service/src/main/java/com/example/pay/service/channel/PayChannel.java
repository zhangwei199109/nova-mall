package com.example.pay.service.channel;

import java.math.BigDecimal;

/**
 * 支付渠道统一抽象：下单预支付、退款。
 * 仅做骨架，具体签名/SDK 调用待接入。
 */
public interface PayChannel {
    /**
        * 渠道标识，如 ALIPAY/WECHAT/UNIONPAY
     */
    String channel();

    /**
     * 预支付下单，返回拉起所需信息（二维码/tn/调起参数等），以 JSON 字符串存储在 extra。
     */
    String prepay(String payNo, BigDecimal amount, String subject);

    /**
     * 退款，返回渠道退款单号。
     */
    String refund(String payNo, BigDecimal amount, String reason);
}

