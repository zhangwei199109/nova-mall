package com.example.order.service.impl;

import com.example.common.exception.BusinessException;
import com.example.order.api.dto.OrderDTO;
import com.example.order.service.OrderAppService;
import com.example.order.service.entity.Order;
import com.example.order.service.mapper.OrderCallbackLogMapper;
import com.example.order.service.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderServiceImplIntegrationTest.TestApp.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yaml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderServiceImplIntegrationTest {

    @SpringBootApplication(scanBasePackages = "com.example.order.service")
    @MapperScan("com.example.order.service.mapper")
    static class TestApp {
    }

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderCallbackLogMapper callbackLogMapper;

    private Order insertOrder(String status) {
        Order order = new Order();
        order.setOrderNo("ORD-" + System.currentTimeMillis());
        order.setUserId(1L);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus(status);
        orderMapper.insert(order);
        return order;
    }

    @Test
    void pay_isIdempotentAfterPaid() {
        Order order = insertOrder("CREATED");

        boolean first = orderAppService.pay(order.getId(), false);
        assertThat(first).isTrue();
        OrderDTO paid = orderAppService.getById(order.getId(), order.getUserId());
        assertThat(paid.getStatus()).isEqualTo("PAID");

        boolean second = orderAppService.pay(order.getId(), false);
        assertThat(second).isTrue();
        OrderDTO stillPaid = orderAppService.getById(order.getId(), order.getUserId());
        assertThat(stillPaid.getStatus()).isEqualTo("PAID");
    }

    @Test
    void payCallback_withDuplicateKeyIsIdempotent() {
        Order order = insertOrder("CREATED");
        // 先插入一条回调幂等记录，模拟重复回调
        com.example.order.service.entity.OrderCallbackLog log = new com.example.order.service.entity.OrderCallbackLog();
        log.setOrderId(order.getId());
        log.setCallbackKey("cb-1");
        callbackLogMapper.insert(log);

        boolean result = orderAppService.pay(order.getId(), true, "cb-1");
        assertThat(result).isTrue();
        // 状态未变化（首次回调因幂等直接返回）
        OrderDTO dto = orderAppService.getById(order.getId(), order.getUserId());
        assertThat(dto.getStatus()).isEqualTo("CREATED");
        assertThat(callbackLogMapper.selectCount(null)).isEqualTo(1L);

        // 正常支付仍可成功
        boolean payResult = orderAppService.pay(order.getId(), false);
        assertThat(payResult).isTrue();
        OrderDTO paid = orderAppService.getById(order.getId(), order.getUserId());
        assertThat(paid.getStatus()).isEqualTo("PAID");
    }

    @Test
    void pay_concurrentOnlyOneSucceeds() throws Exception {
        Order order = insertOrder("CREATED");
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        int threads = 5;
        CountDownLatch latch = new CountDownLatch(threads);

        var pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    boolean ok = orderAppService.pay(order.getId(), false);
                    if (ok) {
                        success.incrementAndGet();
                    }
                } catch (BusinessException e) {
                    if (e.getCode() == 409) {
                        conflict.incrementAndGet();
                    } else {
                        throw e;
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        pool.shutdownNow();

        // 至少一次成功，其余为冲突；并发调度可能使部分线程在已支付后短路成功
        assertThat(success.get()).isGreaterThanOrEqualTo(1);
        assertThat(success.get() + conflict.get()).isEqualTo(threads);

        OrderDTO paid = orderAppService.getById(order.getId(), order.getUserId());
        assertThat(paid.getStatus()).isEqualTo("PAID");
    }
}

