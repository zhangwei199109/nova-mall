package com.example.pay.service;

import com.example.pay.api.dto.*;

public interface PayAppService {
    PaymentDTO createPayment(String idemKey, PaymentCreateRequest req);

    PaymentDTO callback(String payNo, String callbackKey, PaymentCallbackRequest req);

    PaymentDTO detail(String payNo);

    String alipayNotify(String callbackKey, java.util.Map<String, String> params);

    RefundDTO createRefund(String idemKey, RefundCreateRequest req);

    RefundDTO refundCallback(String refundNo, String callbackKey, RefundCallbackRequest req);

    String alipayRefundNotify(String callbackKey, java.util.Map<String, String> params);

    RefundDTO refundDetail(String refundNo);
}

