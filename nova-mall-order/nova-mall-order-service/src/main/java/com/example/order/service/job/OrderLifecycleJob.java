package com.example.order.service.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.order.service.entity.Order;
import com.example.order.service.enums.OrderStatus;
import com.example.order.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单生命周期定时任务（超时未支付自动关闭）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderLifecycleJob {

    private final OrderMapper orderMapper;

    /**
     * 未支付自动关闭的超时时间（分钟）。
     */
    @Value("${order.lifecycle.payment-timeout-minutes:30}")
    private long paymentTimeoutMinutes;

    /**
     * 轮询周期（毫秒）。默认 5 分钟。
     */
    @Value("${order.lifecycle.scan-interval-ms:300000}")
    private long scanIntervalMs;

    /**
     * 扫描未支付超时订单，标记为 CLOSED_TIMEOUT。
     */
    @Scheduled(fixedDelayString = "${order.lifecycle.scan-interval-ms:300000}")
    public void cancelExpiredUnpaidOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
        List<Order> expired = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.CREATED.name())
                .le(Order::getCreateTime, deadline)
                .eq(Order::getDeleted, 0));

        for (Order order : expired) {
            int updated = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, order.getId())
                    .eq(Order::getStatus, OrderStatus.CREATED.name())
                    .eq(Order::getVersion, order.getVersion())
                    .set(Order::getStatus, OrderStatus.CLOSED_TIMEOUT.name())
                    .set(Order::getVersion, order.getVersion() + 1));
            if (updated > 0) {
                log.info("Auto closed order {} due to payment timeout ({} mins).", order.getOrderNo(), paymentTimeoutMinutes);
            }
        }
    }
}




