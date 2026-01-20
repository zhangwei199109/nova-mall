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
 * 自动收货任务：发货后超期未确认收货，自动收货。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderAutoReceiveJob {

    private final OrderMapper orderMapper;

    @Value("${order.lifecycle.auto-receive-days:7}")
    private long autoReceiveDays;

    @Value("${order.lifecycle.scan-interval-ms:300000}")
    private long scanIntervalMs;

    @Scheduled(fixedDelayString = "${order.lifecycle.scan-interval-ms:300000}")
    public void autoFinishShippedOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(autoReceiveDays);
        List<Order> toFinish = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.SHIPPED.name())
                .isNotNull(Order::getShipTime)
                .le(Order::getShipTime, deadline)
                .eq(Order::getDeleted, 0));

        for (Order order : toFinish) {
            int updated = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, order.getId())
                    .eq(Order::getStatus, OrderStatus.SHIPPED.name())
                    .eq(Order::getVersion, order.getVersion())
                    .set(Order::getStatus, OrderStatus.FINISHED.name())
                    .set(Order::getFinishTime, LocalDateTime.now())
                    .set(Order::getVersion, order.getVersion() + 1));
            if (updated > 0) {
                log.info("Auto-finished order {} after {} days of shipment.", order.getOrderNo(), autoReceiveDays);
            }
        }
    }
}






