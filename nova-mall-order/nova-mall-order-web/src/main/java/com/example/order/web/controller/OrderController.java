package com.example.order.web.controller;

import com.example.common.dto.Result;
import com.example.common.exception.BusinessException;
import com.example.order.api.OrderApi;
import com.example.order.service.OrderAppService;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.api.dto.OrderItemDTO;
import com.example.order.service.enums.OrderStatus;
import com.example.product.api.dto.ProductDTO;
import com.example.stock.api.dto.StockChangeDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Duration;

@RestController
@Validated
public class OrderController implements OrderApi {

    private final OrderAppService orderService;
    private final RestTemplate restTemplate;
    @Value("${service.product.base-url:http://localhost:8085}")
    private String productBase;
    @Value("${service.stock.base-url:http://localhost:8087}")
    private String stockBase;
    @Value("${service.cart.base-url:http://localhost:8086}")
    private String cartBase;

    // 简单幂等：10分钟内同 key 返回同一订单
    private final Cache<String, Long> idemCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .build();

    public OrderController(OrderAppService orderService, RestTemplate restTemplate) {
        this.orderService = orderService;
        this.restTemplate = restTemplate;
    }

    @Override
    public Result<List<OrderDTO>> list() {
        return Result.success(orderService.list());
    }

    @Override
    public Result<OrderDTO> detail(@PathVariable Long id) {
        OrderDTO dto = orderService.getById(id);
        if (dto == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(dto);
    }

    @Override
    public Result<OrderDTO> create(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                   @Valid @RequestBody CreateOrderRequest req) {
        if (idemKey != null) {
            Long existed = idemCache.getIfPresent(idemKey);
            if (existed != null) {
                OrderDTO existedOrder = orderService.getById(existed);
                if (existedOrder != null) {
                    return Result.success(existedOrder);
                }
            }
        }
        // 拉取商品信息并校验价格，计算总价
        List<OrderItemDTO> items = req.getItems().stream().map(this::enrichWithProduct).collect(Collectors.toList());
        BigDecimal amount = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 预占库存
        items.forEach(i -> stockCall("/reserve", i.getProductId(), i.getQuantity()));

        OrderDTO dto = new OrderDTO();
        dto.setOrderNo(generateOrderNo());
        dto.setUserId(req.getUserId());
        dto.setAmount(amount);
        dto.setStatus(OrderStatus.CREATED.name());
        dto.setItems(items);
        OrderDTO saved = orderService.create(req, dto);
        if (idemKey != null) {
            idemCache.put(idemKey, saved.getId());
        }
        return Result.success(saved);
    }

    @Override
    public Result<Boolean> pay(@PathVariable Long id) {
        return payInternal(id, false);
    }

    @Override
    public Result<Boolean> payCallback(@PathVariable Long id) {
        return payInternal(id, true);
    }

    @Override
    public Result<Boolean> cancel(@PathVariable Long id) {
        OrderDTO order = orderService.getById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        OrderStatus status = OrderStatus.valueOf(order.getStatus());
        if (!OrderStatus.canCancel(status)) {
            return Result.error(400, "订单状态不允许取消");
        }
        if (order.getItems() != null) {
            order.getItems().forEach(i -> stockCall("/release", i.getProductId(), i.getQuantity()));
        }
        orderService.updateStatus(id, OrderStatus.CANCELLED.name());
        return Result.success(true);
    }

    @Override
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = orderService.delete(id);
        if (!ok) {
            return Result.error(404, "订单不存在");
        }
        return Result.success();
    }

    private OrderItemDTO enrichWithProduct(OrderItemDTO item) {
        String url = productBase + "/product/" + item.getProductId();
        ResponseEntity<Result<ProductDTO>> resp = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<Result<ProductDTO>>() {});
        Result<ProductDTO> body = resp.getBody();
        if (body == null || body.getCode() != 200 || body.getData() == null) {
            throw new BusinessException(400, "商品不存在: " + item.getProductId());
        }
        ProductDTO p = body.getData();
        OrderItemDTO enriched = new OrderItemDTO();
        enriched.setProductId(p.getId());
        enriched.setProductName(p.getName());
        enriched.setPrice(p.getPrice());
        enriched.setQuantity(item.getQuantity());
        return enriched;
    }

    private void stockCall(String path, Long productId, Integer qty) {
        StockChangeDTO dto = new StockChangeDTO();
        dto.setProductId(productId);
        dto.setQuantity(qty);
        String url = stockBase + "/stock" + path;
        ResponseEntity<Result<Boolean>> resp = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(dto, jsonHeaders()),
                new ParameterizedTypeReference<Result<Boolean>>() {});
        Result<Boolean> body = resp.getBody();
        if (body == null || body.getCode() != 200 || Boolean.FALSE.equals(body.getData())) {
            throw new BusinessException(400, "库存操作失败: " + path);
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String generateOrderNo() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private Result<Boolean> payInternal(Long id, boolean fromCallback) {
        OrderDTO order = orderService.getById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        OrderStatus status = OrderStatus.valueOf(order.getStatus());
        if (!OrderStatus.canPay(status)) {
            return Result.success(true); // 幂等返回
        }
        if (order.getItems() != null) {
            order.getItems().forEach(i -> stockCall("/deduct", i.getProductId(), i.getQuantity()));
        }
        orderService.updateStatus(id, OrderStatus.PAID.name());
        // 支付成功后清理购物车中对应商品
        clearCart(order.getUserId(), order.getItems());
        return Result.success(true);
    }

    private void clearCart(Long userId, List<OrderItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        HttpHeaders headers = jsonHeaders();
        headers.add("X-User-Id", String.valueOf(userId));
        items.forEach(i -> {
            String url = cartBase + "/cart/" + i.getProductId();
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), new ParameterizedTypeReference<Result<Boolean>>() {});
        });
    }
}

