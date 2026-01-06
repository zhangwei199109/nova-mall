package com.example.order.service.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.order.service.entity.OrderInventoryTask;
import com.example.order.service.mapper.OrderInventoryTaskMapper;
import com.example.product.api.ProductApi;
import com.example.product.api.dto.ProductAdjustRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 异步扣减库存与销量累加任务（支付成功后补偿处理）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderInventoryDeductJob {

    private final OrderInventoryTaskMapper taskMapper;
    private final ProductApi productApi;
    private final JdbcTemplate jdbcTemplate;

    @Value("${order.inventory-deduct.max-retry:5}")
    private int maxRetry;

    @Value("${order.inventory-deduct.batch-size:50}")
    private int batchSize;

    @PostConstruct
    public void ensureTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS order_inventory_tasks (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  order_id BIGINT NOT NULL,
                  product_id BIGINT NOT NULL,
                  quantity INT NOT NULL,
                  status VARCHAR(32) NOT NULL DEFAULT 'INIT',
                  retry_count INT NOT NULL DEFAULT 0,
                  last_error VARCHAR(500),
                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  UNIQUE KEY uk_order_product (order_id, product_id)
                )
                """);
    }

    @Scheduled(fixedDelayString = "${order.inventory-deduct.scan-interval-ms:30000}")
    public void processTasks() {
        List<OrderInventoryTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<OrderInventoryTask>()
                .in(OrderInventoryTask::getStatus, List.of("INIT", "FAILED"))
                .lt(OrderInventoryTask::getRetryCount, maxRetry)
                .orderByAsc(OrderInventoryTask::getId)
                .last("limit " + batchSize));
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        for (OrderInventoryTask task : tasks) {
            try {
                ProductAdjustRequest req = new ProductAdjustRequest();
                req.setProductId(task.getProductId());
                req.setQuantity(task.getQuantity());
                var resp = productApi.adjustAfterPay(List.of(req));
                boolean ok = resp != null && resp.getCode() != null && resp.getCode() == 200;
                if (ok) {
                    taskMapper.update(null, new LambdaUpdateWrapper<OrderInventoryTask>()
                            .eq(OrderInventoryTask::getId, task.getId())
                            .set(OrderInventoryTask::getStatus, "SUCCESS")
                            .set(OrderInventoryTask::getRetryCount, task.getRetryCount() + 1)
                            .set(OrderInventoryTask::getLastError, null));
                } else {
                    String msg = resp == null ? "null response" : resp.getMessage();
                    fail(task, msg);
                }
            } catch (Exception ex) {
                fail(task, ex.getMessage());
            }
        }
    }

    private void fail(OrderInventoryTask task, String msg) {
        taskMapper.update(null, new LambdaUpdateWrapper<OrderInventoryTask>()
                .eq(OrderInventoryTask::getId, task.getId())
                .set(OrderInventoryTask::getStatus, "FAILED")
                .set(OrderInventoryTask::getRetryCount, task.getRetryCount() + 1)
                .set(OrderInventoryTask::getLastError, truncate(msg, 480)));
        log.warn("Inventory deduct task failed orderId={}, productId={}, retry={}, msg={}",
                task.getOrderId(), task.getProductId(), task.getRetryCount() + 1, msg);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

