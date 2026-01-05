package com.example.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.common.exception.BusinessException;
import com.example.pay.api.dto.*;
import com.example.pay.service.PayAppService;
import com.example.pay.service.entity.Payment;
import com.example.pay.service.entity.Refund;
import com.example.pay.service.mapper.PaymentMapper;
import com.example.pay.service.mapper.PaymentCallbackLogMapper;
import com.example.pay.service.mapper.RefundMapper;
import com.example.pay.service.mapper.RefundCallbackLogMapper;
import com.example.order.api.OrderApi;
import com.example.order.api.dto.OrderStatusUpdateRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PayAppServiceImpl implements PayAppService {

    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;
    private final PaymentCallbackLogMapper paymentCallbackLogMapper;
    private final RefundCallbackLogMapper refundCallbackLogMapper;
    private final com.example.pay.service.channel.AlipayChannelClient alipayChannelClient;
    private final OrderApi orderApi;

    public PayAppServiceImpl(PaymentMapper paymentMapper, RefundMapper refundMapper,
                            PaymentCallbackLogMapper paymentCallbackLogMapper,
                            RefundCallbackLogMapper refundCallbackLogMapper,
                            com.example.pay.service.channel.AlipayChannelClient alipayChannelClient,
                            OrderApi orderApi) {
        this.paymentMapper = paymentMapper;
        this.refundMapper = refundMapper;
        this.paymentCallbackLogMapper = paymentCallbackLogMapper;
        this.refundCallbackLogMapper = refundCallbackLogMapper;
        this.alipayChannelClient = alipayChannelClient;
        this.orderApi = orderApi;
    }

    @Override
    @Transactional
    public PaymentDTO createPayment(String idemKey, PaymentCreateRequest req) {
        if (req == null || req.getOrderId() == null) {
            throw new BusinessException(400, "订单ID不能为空");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            throw new BusinessException(400, "金额必须大于0");
        }
        if (StringUtils.hasText(idemKey)) {
            Payment existed = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getIdemKey, idemKey));
            if (existed != null) {
                return toDTO(existed);
            }
        }
        Payment payment = new Payment();
        payment.setPayNo(genNo("PAY"));
        payment.setOrderId(req.getOrderId());
        payment.setUserId(null); // 可在调用方透传并填充
        payment.setAmount(req.getAmount());
        payment.setChannel("ALIPAY");
        payment.setCurrency("CNY");
        String qr = alipayChannelClient.precreate(payment.getPayNo(), payment.getAmount(), "ORDER-" + req.getOrderId());
        payment.setExtra("{\"qrCode\":\"" + qr + "\"}");
        payment.setStatus("INIT");
        payment.setIdemKey(StringUtils.hasText(idemKey) ? idemKey : null);
        try {
            paymentMapper.insert(payment);
        } catch (DuplicateKeyException e) {
            if (StringUtils.hasText(idemKey)) {
                Payment existed = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getIdemKey, idemKey));
                if (existed != null) {
                    return toDTO(existed);
                }
            }
            throw e;
        }
        return toDTO(paymentMapper.selectById(payment.getId()));
    }

    @Override
    @Transactional
    public PaymentDTO callback(String payNo, String callbackKey, PaymentCallbackRequest req) {
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo)
                .eq(Payment::getDeleted, 0));
        if (payment == null) {
            throw new BusinessException(404, "支付单不存在");
        }
        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            return toDTO(payment);
        }
        if (StringUtils.hasText(callbackKey)) {
            try {
                com.example.pay.service.entity.PaymentCallbackLog log = new com.example.pay.service.entity.PaymentCallbackLog();
                log.setPayNo(payNo);
                log.setCallbackKey(callbackKey);
                log.setRawMessage(req == null ? null : req.getMessage());
                paymentCallbackLogMapper.insert(log);
            } catch (DuplicateKeyException e) {
                return toDTO(payment); // 回调幂等
            }
        }
        int updated = paymentMapper.update(null, new LambdaUpdateWrapper<Payment>()
                .eq(Payment::getId, payment.getId())
                .eq(Payment::getStatus, "INIT")
                .set(Payment::getStatus, "SUCCESS")
                .set(Payment::getChannelTradeNo, req == null ? null : req.getChannelCode())
                .set(Payment::getVersion, payment.getVersion() + 1));
        if (updated == 0) {
            // 并发或状态冲突
            payment = paymentMapper.selectById(payment.getId());
        } else {
            payment = paymentMapper.selectById(payment.getId());
            // 回写订单已支付
            notifyOrderPaid(payment);
        }
        return toDTO(payment);
    }

    @Override
    @Transactional
    public String alipayNotify(String callbackKey, Map<String, String> params) {
        com.example.pay.service.channel.AlipayNotifyResult parsed = alipayChannelClient.parseAndVerifyNotify(params, callbackKey);
        if (parsed == null) {
            return "fail";
        }
        String payNo = parsed.getPayNo();
        String tradeNo = parsed.getTradeNo();
        String tradeStatus = parsed.getTradeStatus();
        if (!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {
            return "fail";
        }
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo)
                .eq(Payment::getDeleted, 0));
        if (payment == null) {
            return "fail";
        }
        if (payment.getAmount() != null && parsed.getAmount() != null
                && parsed.getAmount().compareTo(payment.getAmount()) != 0) {
            return "fail";
        }
        String cbKey = StringUtils.hasText(callbackKey) ? callbackKey : (params.getOrDefault("notify_id", "") + tradeNo);
        if (StringUtils.hasText(cbKey)) {
            try {
                com.example.pay.service.entity.PaymentCallbackLog log = new com.example.pay.service.entity.PaymentCallbackLog();
                log.setPayNo(payNo);
                log.setCallbackKey(cbKey);
                log.setRawMessage(params == null ? null : params.toString());
                paymentCallbackLogMapper.insert(log);
            } catch (DuplicateKeyException e) {
                return "success";
            }
        }
        int updated = paymentMapper.update(null, new LambdaUpdateWrapper<Payment>()
                .eq(Payment::getId, payment.getId())
                .eq(Payment::getStatus, "INIT")
                .set(Payment::getStatus, "SUCCESS")
                .set(Payment::getChannelTradeNo, tradeNo)
                .set(Payment::getVersion, payment.getVersion() + 1));
        if (updated == 0) {
            return "success";
        }
        return "success";
    }

    @Override
    public PaymentDTO detail(String payNo) {
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo));
        if (payment == null) {
            throw new BusinessException(404, "支付单不存在");
        }
        return toDTO(payment);
    }

    @Override
    @Transactional
    public RefundDTO createRefund(String idemKey, RefundCreateRequest req) {
        if (req == null || !StringUtils.hasText(req.getPayNo())) {
            throw new BusinessException(400, "支付单号不能为空");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            throw new BusinessException(400, "退款金额必须大于0");
        }
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, req.getPayNo())
                .eq(Payment::getDeleted, 0));
        if (payment == null) {
            throw new BusinessException(404, "支付单不存在");
        }
        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new BusinessException(400, "支付未成功，无法退款");
        }
        BigDecimal refunded = refundedAmount(payment.getPayNo());
        if (refunded.add(req.getAmount()).compareTo(payment.getAmount()) > 0) {
            throw new BusinessException(400, "退款金额超过可退余额");
        }
        if (StringUtils.hasText(idemKey)) {
            Refund existed = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                    .eq(Refund::getIdemKey, idemKey));
            if (existed != null) {
                return toDTO(existed);
            }
        }
        Refund refund = new Refund();
        refund.setRefundNo(genNo("REF"));
        refund.setPayNo(payment.getPayNo());
        refund.setOrderId(payment.getOrderId());
        refund.setUserId(payment.getUserId());
        refund.setAmount(req.getAmount());
        refund.setStatus("INIT");
        refund.setReason(req.getReason());
        refund.setIdemKey(StringUtils.hasText(idemKey) ? idemKey : null);
        refund.setCurrency("CNY");
        try {
            refundMapper.insert(refund);
        } catch (DuplicateKeyException e) {
            if (StringUtils.hasText(idemKey)) {
                Refund existed = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                        .eq(Refund::getIdemKey, idemKey));
                if (existed != null) {
                    return toDTO(existed);
                }
            }
            throw e;
        }
        // 调用渠道退款
        String channelRefundNo = alipayChannelClient.refund(payment.getPayNo(), req.getAmount(), req.getReason());
        if (channelRefundNo != null) {
            refund.setChannelRefundNo(channelRefundNo);
            refundMapper.updateById(refund);
        }
        return toDTO(refundMapper.selectById(refund.getId()));
    }

    @Override
    @Transactional
    public RefundDTO refundCallback(String refundNo, String callbackKey, RefundCallbackRequest req) {
        Refund refund = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getRefundNo, refundNo)
                .eq(Refund::getDeleted, 0));
        if (refund == null) {
            throw new BusinessException(404, "退款单不存在");
        }
        if ("SUCCESS".equalsIgnoreCase(refund.getStatus())) {
            return toDTO(refund);
        }
        if (StringUtils.hasText(callbackKey)) {
            try {
                com.example.pay.service.entity.RefundCallbackLog log = new com.example.pay.service.entity.RefundCallbackLog();
                log.setRefundNo(refundNo);
                log.setCallbackKey(callbackKey);
                log.setRawMessage(req == null ? null : req.getMessage());
                refundCallbackLogMapper.insert(log);
            } catch (DuplicateKeyException e) {
                return toDTO(refund);
            }
        }
        int updated = refundMapper.update(null, new LambdaUpdateWrapper<Refund>()
                .eq(Refund::getId, refund.getId())
                .eq(Refund::getStatus, "INIT")
                .set(Refund::getStatus, "SUCCESS")
                .set(Refund::getVersion, refund.getVersion() + 1));
        if (updated == 0) {
            refund = refundMapper.selectById(refund.getId());
        } else {
            refund = refundMapper.selectById(refund.getId());
            BigDecimal refundedTotal = refundedAmount(refund.getPayNo());
            boolean fully = refundedTotal.compareTo(paymentAmountOrZero(refund.getPayNo())) >= 0;
            notifyOrderRefunded(refund, fully);
        }
        return toDTO(refund);
    }

    @Override
    public RefundDTO refundDetail(String refundNo) {
        Refund refund = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getRefundNo, refundNo)
                .eq(Refund::getDeleted, 0));
        if (refund == null) {
            throw new BusinessException(404, "退款单不存在");
        }
        return toDTO(refund);
    }

    @Override
    @Transactional
    public String alipayRefundNotify(String callbackKey, Map<String, String> params) {
        com.example.pay.service.channel.AlipayRefundNotifyResult parsed = alipayChannelClient.parseRefundNotify(params, callbackKey);
        if (parsed == null || !"REFUND_SUCCESS".equalsIgnoreCase(parsed.getStatus())) {
            return "fail";
        }
        Refund refund = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getRefundNo, parsed.getRefundNo())
                .eq(Refund::getDeleted, 0));
        if (refund == null) {
            return "fail";
        }
        if (parsed.getAmount() != null && refund.getAmount() != null
                && parsed.getAmount().compareTo(refund.getAmount()) != 0) {
            return "fail";
        }
        String cbKey = StringUtils.hasText(callbackKey) ? callbackKey : (params.getOrDefault("notify_id", "") + parsed.getChannelRefundNo());
        if (StringUtils.hasText(cbKey)) {
            try {
                com.example.pay.service.entity.RefundCallbackLog log = new com.example.pay.service.entity.RefundCallbackLog();
                log.setRefundNo(refund.getRefundNo());
                log.setCallbackKey(cbKey);
                log.setRawMessage(params == null ? null : params.toString());
                refundCallbackLogMapper.insert(log);
            } catch (DuplicateKeyException e) {
                return "success";
            }
        }
        int updated = refundMapper.update(null, new LambdaUpdateWrapper<Refund>()
                .eq(Refund::getId, refund.getId())
                .eq(Refund::getStatus, "INIT")
                .set(Refund::getStatus, "SUCCESS")
                .set(Refund::getChannelRefundNo, parsed.getChannelRefundNo())
                .set(Refund::getVersion, refund.getVersion() + 1));
        if (updated == 0) {
            return "success";
        }
        Refund fresh = refundMapper.selectById(refund.getId());
        BigDecimal refundedTotal = refundedAmount(fresh.getPayNo());
        boolean fully = refundedTotal.compareTo(paymentAmountOrZero(fresh.getPayNo())) >= 0;
        notifyOrderRefunded(fresh, fully);
        return "success";
    }

    private BigDecimal refundedAmount(String payNo) {
        List<Refund> list = refundMapper.selectList(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getPayNo, payNo)
                .eq(Refund::getStatus, "SUCCESS")
                .eq(Refund::getDeleted, 0));
        return list.stream()
                .map(r -> r.getAmount() == null ? BigDecimal.ZERO : r.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentDTO toDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        BeanUtils.copyProperties(payment, dto);
        return dto;
    }

    private RefundDTO toDTO(Refund refund) {
        RefundDTO dto = new RefundDTO();
        BeanUtils.copyProperties(refund, dto);
        return dto;
    }

    private String genNo(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void notifyOrderPaid(Payment payment) {
        try {
            OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
            req.setStatus("PAID");
            orderApi.updateStatus(payment.getOrderId(), req);
        } catch (Exception e) {
            // ignore to avoid blocking callback; can log or send async event
        }
    }

    private void notifyOrderRefunded(Refund refund, boolean fullyRefunded) {
        try {
            OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
            req.setStatus(fullyRefunded ? "REFUNDED" : "PARTIAL_REFUND");
            orderApi.updateStatus(refund.getOrderId(), req);
        } catch (Exception e) {
            // ignore
        }
    }

    private BigDecimal paymentAmountOrZero(String payNo) {
        Payment p = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo)
                .eq(Payment::getDeleted, 0));
        if (p == null || p.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        return p.getAmount();
    }
}

