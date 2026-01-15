package com.example.order.web.service;

import com.example.common.exception.BusinessException;
import com.example.common.dto.Result;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.api.dto.OrderItemDTO;
import com.example.order.api.dto.SeckillActivityDTO;
import com.example.order.api.dto.SeckillOrderResultDTO;
import com.example.order.api.dto.SeckillPlaceRequest;
import com.example.order.web.client.OpsAdminApi;
import com.example.order.web.client.dto.OpsActivityDTO;
import com.example.order.service.OrderAppService;
import com.example.product.api.ProductApi;
import com.example.product.api.dto.ProductDTO;
import com.example.stock.api.StockApi;
import com.example.stock.api.dto.StockChangeDTO;
import com.example.order.web.config.SeckillProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private final OrderAppService orderAppService;
    private final ProductApi productApi;
    private final StockApi stockApi;
    private final SeckillProperties seckillProperties;
    private final OpsAdminApi opsAdminApi;

    private final Map<Long, SeckillActivity> activities = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> remain = new ConcurrentHashMap<>();
    private final Map<String, SeckillOrderResultDTO> userOrders = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (loadFromOps()) {
            return;
        }
        if (loadFromConfig()) {
            return;
        }
        loadDefaultExamples();
    }

    public List<SeckillActivityDTO> activities() {
        List<SeckillActivityDTO> list = new ArrayList<>();
        activities.values().stream()
                .sorted(Comparator.comparing(SeckillActivity::id))
                .forEach(a -> list.add(toDTO(a)));
        return list;
    }

    public SeckillOrderResultDTO place(Long userId, String idemKey, SeckillPlaceRequest req) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SeckillActivity activity = getActivity(req.getActivityId());
        int qty = (req.getQuantity() == null || req.getQuantity() < 1) ? 1 : req.getQuantity();
        if (qty > activity.limitPerUser()) {
            throw new BusinessException(400, "单用户限购 " + activity.limitPerUser() + " 件");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.startTime())) {
            throw new BusinessException(400, "活动尚未开始");
        }
        if (now.isAfter(activity.endTime())) {
            throw new BusinessException(400, "活动已结束");
        }
        String userKey = userKey(userId, activity.id());
        SeckillOrderResultDTO existed = userOrders.get(userKey);
        if (existed != null) {
            return existed;
        }
        AtomicInteger counter = remain.get(activity.id());
        if (counter == null) {
            throw new BusinessException(404, "库存未初始化");
        }
        // 原子扣减剩余秒杀库存
        reserveStock(counter, qty);

        StockChangeDTO change = new StockChangeDTO();
        change.setProductId(activity.productId());
        change.setQuantity(qty);
        boolean stockReserved = false;
        try {
            ProductDTO product = fetchProduct(activity.productId());
            callStockReserve(change);
            stockReserved = true;
            OrderDTO order = createPaidOrder(userId, idemKey, activity, product, qty);
            callStockDeduct(change);
            SeckillOrderResultDTO result = buildSuccess(activity, order, qty);
            userOrders.put(userKey, result);
            return result;
        } catch (BusinessException e) {
            rollback(counter, qty, stockReserved, change);
            throw e;
        } catch (Exception e) {
            rollback(counter, qty, stockReserved, change);
            throw new BusinessException(500, "秒杀下单失败: " + e.getMessage(), e);
        }
    }

    public SeckillOrderResultDTO result(Long userId, Long activityId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        String userKey = userKey(userId, activityId);
        SeckillOrderResultDTO existed = userOrders.get(userKey);
        if (existed != null) {
            return existed;
        }
        SeckillOrderResultDTO pending = new SeckillOrderResultDTO();
        pending.setActivityId(activityId);
        pending.setStatus("PENDING");
        pending.setMessage("尚未下单或处理中");
        return pending;
    }

    private void register(SeckillActivity activity) {
        activities.put(activity.id(), activity);
        remain.put(activity.id(), new AtomicInteger(activity.totalStock()));
    }

    private SeckillActivity getActivity(Long id) {
        if (id == null) {
            throw new BusinessException(400, "活动ID不能为空");
        }
        SeckillActivity activity = activities.get(id);
        if (activity == null) {
            throw new BusinessException(404, "秒杀活动不存在");
        }
        return activity;
    }

    private void reserveStock(AtomicInteger counter, int qty) {
        while (true) {
            int current = counter.get();
            if (current < qty) {
                throw new BusinessException(410, "秒杀库存不足");
            }
            if (counter.compareAndSet(current, current - qty)) {
                return;
            }
        }
    }

    private ProductDTO fetchProduct(Long productId) {
        Result<ProductDTO> resp = productApi.get(productId);
        if (resp == null || resp.getCode() == null || resp.getCode() != 200 || resp.getData() == null) {
            throw new BusinessException(404, "商品不存在或查询失败");
        }
        ProductDTO dto = resp.getData();
        if (dto.getStatus() != null && dto.getStatus() == 0) {
            throw new BusinessException(410, "商品已下架");
        }
        return dto;
    }

    private void callStockReserve(StockChangeDTO change) {
        Result<Boolean> resp = stockApi.reserve(change);
        if (resp == null || resp.getCode() == null || resp.getCode() != 200 || !Boolean.TRUE.equals(resp.getData())) {
            throw new BusinessException(409, "预占库存失败");
        }
    }

    private void callStockDeduct(StockChangeDTO change) {
        Result<Boolean> resp = stockApi.deduct(change);
        if (resp == null || resp.getCode() == null || resp.getCode() != 200 || !Boolean.TRUE.equals(resp.getData())) {
            throw new BusinessException(409, "扣减库存失败");
        }
    }

    private OrderDTO createPaidOrder(Long userId, String idemKey, SeckillActivity activity, ProductDTO product, int qty) {
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(activity.productId());
        item.setProductName(product.getName());
        item.setPrice(activity.seckillPrice());
        item.setQuantity(qty);
        CreateOrderRequest req = new CreateOrderRequest();
        req.setUserId(userId);
        req.setItems(List.of(item));
        OrderDTO order = orderAppService.createOrder(idemKey, req);
        orderAppService.pay(order.getId(), userId, false);
        return orderAppService.getById(order.getId(), userId);
    }

    private SeckillOrderResultDTO buildSuccess(SeckillActivity activity, OrderDTO order, int qty) {
        SeckillOrderResultDTO dto = new SeckillOrderResultDTO();
        dto.setActivityId(activity.id());
        dto.setOrderId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setQuantity(qty);
        dto.setPayAmount(activity.seckillPrice().multiply(BigDecimal.valueOf(qty)));
        dto.setStatus("SUCCESS");
        dto.setMessage("下单成功");
        return dto;
    }

    private void rollback(AtomicInteger counter, int qty, boolean stockReserved, StockChangeDTO change) {
        counter.addAndGet(qty);
        if (stockReserved) {
            try {
                stockApi.release(change);
            } catch (Exception ex) {
                log.warn("释放预占库存失败, productId={}, qty={}", change.getProductId(), change.getQuantity(), ex);
            }
        }
    }

    private SeckillActivityDTO toDTO(SeckillActivity a) {
        SeckillActivityDTO dto = new SeckillActivityDTO();
        dto.setId(a.id());
        dto.setProductId(a.productId());
        dto.setTitle(a.title());
        dto.setSeckillPrice(a.seckillPrice());
        dto.setTotalStock(a.totalStock());
        AtomicInteger left = remain.get(a.id());
        dto.setStockLeft(left == null ? 0 : left.get());
        dto.setLimitPerUser(a.limitPerUser());
        dto.setStartTime(a.startTime());
        dto.setEndTime(a.endTime());
        dto.setStatus(calcStatus(a));
        return dto;
    }

    private String calcStatus(SeckillActivity a) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(a.startTime())) {
            return "NOT_STARTED";
        }
        if (now.isAfter(a.endTime())) {
            return "ENDED";
        }
        return "ONGOING";
    }

    private String userKey(Long userId, Long activityId) {
        return userId + ":" + activityId;
    }

    private int nvl(Integer v, int def) {
        return v == null ? def : v;
    }

    private boolean loadFromOps() {
        try {
            Result<List<OpsActivityDTO>> resp = opsAdminApi.listActive();
            if (resp == null || resp.getCode() == null || resp.getCode() != 200 || resp.getData() == null) {
                return false;
            }
            List<OpsActivityDTO> data = resp.getData();
            if (data.isEmpty()) {
                return false;
            }
            data.forEach(a -> register(new SeckillActivity(
                    a.getId(),
                    a.getProductId(),
                    a.getTitle(),
                    a.getSeckillPrice(),
                    nvl(a.getTotalStock(), 0),
                    nvl(a.getLimitPerUser(), 1),
                    a.getStartTime(),
                    a.getEndTime()
            )));
            log.info("Seckill activities loaded from ops-admin, size={}", data.size());
            return true;
        } catch (Exception e) {
            log.warn("Load seckill activities from ops-admin failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean loadFromConfig() {
        List<SeckillProperties.Activity> configured = seckillProperties.getActivities();
        if (configured != null && !configured.isEmpty()) {
            configured.forEach(a -> register(new SeckillActivity(
                    a.getId(),
                    a.getProductId(),
                    a.getTitle(),
                    a.getSeckillPrice(),
                    nvl(a.getTotalStock(), 0),
                    nvl(a.getLimitPerUser(), 1),
                    a.getStartTime(),
                    a.getEndTime()
            )));
            log.info("Seckill activities loaded from config, size={}", configured.size());
            return true;
        }
        return false;
    }

    private void loadDefaultExamples() {
        LocalDateTime now = LocalDateTime.now();
        register(new SeckillActivity(1L, 1L, "iPhone 15 秒杀", new BigDecimal("5999.00"),
                30, 1, now.minusMinutes(10), now.plusDays(1)));
        register(new SeckillActivity(2L, 3L, "AirPods Pro 闪购", new BigDecimal("999.00"),
                50, 2, now.minusMinutes(10), now.plusDays(1)));
        log.info("Seckill activities loaded by default examples");
    }

    private record SeckillActivity(Long id, Long productId, String title, BigDecimal seckillPrice, int totalStock,
                                   int limitPerUser, LocalDateTime startTime, LocalDateTime endTime) {
    }
}

