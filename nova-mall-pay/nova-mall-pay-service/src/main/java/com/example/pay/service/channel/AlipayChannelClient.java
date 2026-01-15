package com.example.pay.service.channel;

import java.math.BigDecimal;
import java.util.Map;

public interface AlipayChannelClient {

    /**
     * 预下单，返回二维码或支付表单链接。
     */
    String precreate(String payNo, BigDecimal amount, String subject);

    /**
     * 验签并解析回调；验签失败返回 null。
     */
    AlipayNotifyResult parseAndVerifyNotify(Map<String, String> params, String callbackKey);

    /**
     * 发起退款，返回渠道退款单号或 null。
     */
    String refund(String payNo, BigDecimal amount, String reason);

    /**
     * 验签并解析退款回调；验签失败返回 null。
     */
    com.example.pay.service.channel.AlipayRefundNotifyResult parseRefundNotify(Map<String, String> params, String callbackKey);
}

