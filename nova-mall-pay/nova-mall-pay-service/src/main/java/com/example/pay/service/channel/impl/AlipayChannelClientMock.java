package com.example.pay.service.channel.impl;

import com.example.pay.service.channel.AlipayChannelClient;
import com.example.pay.service.channel.AlipayNotifyResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Mock 渠道实现：无真实验签，只校验必填字段并生成二维码占位。
 * 后续可替换为正式 Alipay SDK 实现。
 */
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@Component("alipayChannelClientMock")
@ConditionalOnMissingBean(AlipayChannelClient.class)
public class AlipayChannelClientMock implements AlipayChannelClient {
    @Override
    public String precreate(String payNo, BigDecimal amount, String subject) {
        return "https://mock.alipay.com/qr/" + payNo;
    }

    @Override
    public AlipayNotifyResult parseAndVerifyNotify(Map<String, String> params, String callbackKey) {
        if (params == null) {
            return null;
        }
        String outTradeNo = params.getOrDefault("out_trade_no", null);
        String tradeNo = params.getOrDefault("trade_no", UUID.randomUUID().toString());
        String status = params.getOrDefault("trade_status", "TRADE_SUCCESS");
        String totalAmountStr = params.getOrDefault("total_amount", null);
        if (outTradeNo == null) {
            return null;
        }
        AlipayNotifyResult r = new AlipayNotifyResult();
        r.setPayNo(outTradeNo);
        r.setTradeNo(tradeNo);
        r.setTradeStatus(status);
        if (totalAmountStr != null) {
            try {
                r.setAmount(new BigDecimal(totalAmountStr));
            } catch (Exception ignored) {
            }
        }
        return r;
    }

    @Override
    public String refund(String payNo, BigDecimal amount, String reason) {
        return "REF-MOCK-" + payNo;
    }

    @Override
    public com.example.pay.service.channel.AlipayRefundNotifyResult parseRefundNotify(Map<String, String> params, String callbackKey) {
        if (params == null) {
            return null;
        }
        com.example.pay.service.channel.AlipayRefundNotifyResult r = new com.example.pay.service.channel.AlipayRefundNotifyResult();
        r.setRefundNo(params.getOrDefault("out_request_no", "REF-" + UUID.randomUUID()));
        r.setPayNo(params.getOrDefault("out_trade_no", null));
        r.setChannelRefundNo(params.getOrDefault("trade_no", "MOCK-REF"));
        String amountStr = params.get("refund_amount");
        if (amountStr != null) {
            try {
                r.setAmount(new BigDecimal(amountStr));
            } catch (Exception ignore) {
            }
        }
        r.setStatus(params.getOrDefault("refund_status", "REFUND_SUCCESS"));
        if (r.getPayNo() == null) {
            return null;
        }
        return r;
    }
}

