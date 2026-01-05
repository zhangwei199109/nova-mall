package com.example.pay.service.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.pay.service.entity.Payment;
import com.example.pay.service.entity.Refund;
import com.example.pay.service.mapper.PaymentMapper;
import com.example.pay.service.mapper.RefundMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 简单对账/超时兜底任务（Mock 场景）：对长时间未更新的 INIT 单据做超时关闭/失败标记。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PayReconcileJob {

    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;

    /**
     * 每5分钟扫描一次，15分钟未完成的支付关闭。
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void closeStalePayments() {
        LocalDateTime threshold = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);
        List<Payment> list = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getStatus, "INIT")
                .eq(Payment::getDeleted, 0)
                .le(Payment::getCreateTime, threshold));
        for (Payment p : list) {
            int updated = paymentMapper.update(null, new LambdaUpdateWrapper<Payment>()
                    .eq(Payment::getId, p.getId())
                    .eq(Payment::getStatus, "INIT")
                    .set(Payment::getStatus, "CLOSED")
                    .set(Payment::getVersion, p.getVersion() == null ? 1 : p.getVersion() + 1));
            if (updated > 0) {
                log.info("Closed stale payment {}, created at {}", p.getPayNo(), p.getCreateTime());
            }
        }
    }

    /**
     * 每5分钟扫描一次，15分钟未完成的退款标记失败。
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 30 * 1000L)
    public void failStaleRefunds() {
        LocalDateTime threshold = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);
        List<Refund> list = refundMapper.selectList(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getStatus, "INIT")
                .eq(Refund::getDeleted, 0)
                .le(Refund::getCreateTime, threshold));
        for (Refund r : list) {
            int updated = refundMapper.update(null, new LambdaUpdateWrapper<Refund>()
                    .eq(Refund::getId, r.getId())
                    .eq(Refund::getStatus, "INIT")
                    .set(Refund::getStatus, "FAILED")
                    .set(Refund::getVersion, r.getVersion() == null ? 1 : r.getVersion() + 1));
            if (updated > 0) {
                log.info("Mark refund failed (stale) {}, created at {}", r.getRefundNo(), r.getCreateTime());
            }
        }
    }
}

