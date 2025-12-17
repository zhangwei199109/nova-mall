package com.example.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cart.api.CartApi;
import com.example.cart.api.dto.CartItemDTO;
import com.example.common.dto.Result;
import com.example.common.exception.BusinessException;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.api.dto.OrderItemDTO;
import com.example.order.service.convert.OrderConvert;
import com.example.order.service.OrderAppService;
import com.example.order.service.entity.Order;
import com.example.order.service.entity.OrderItem;
import com.example.order.service.enums.OrderStatus;
import com.example.order.service.mapper.OrderItemMapper;
import com.example.order.service.mapper.OrderMapper;
import com.example.product.api.ProductApi;
import com.example.product.api.dto.ProductDTO;
import com.example.stock.api.StockApi;
import com.example.stock.api.dto.StockChangeDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderAppService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderConvert orderConvert;
    private final ProductApi productApi;
    private final StockApi stockApi;
    private final CartApi cartApi;
    // 简单幂等：10分钟内同 key 返回同一订单
    private final Cache<String, Long> idemCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .build();

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            OrderConvert orderConvert,
                            ProductApi productApi,
                            StockApi stockApi,
                            CartApi cartApi) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderConvert = orderConvert;
        this.productApi = productApi;
        this.stockApi = stockApi;
        this.cartApi = cartApi;
    }

    @Override
    public List<OrderDTO> list() {
        return orderMapper.selectList(null).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getById(Long id) {
        Order order = orderMapper.selectById(id);
        return order == null ? null : toDTO(order);
    }

    @Override
    public OrderDTO createOrder(String idemKey, CreateOrderRequest req) {
        if (idemKey != null) {
            Long existed = idemCache.getIfPresent(idemKey);
            if (existed != null) {
                OrderDTO existedOrder = getById(existed);
                if (existedOrder != null) {
                    return existedOrder;
                }
            }
        }
        // 准备订单项：若未传入，则从购物车拉取
        List<OrderItemDTO> items = resolveItems(req);
        BigDecimal amount = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItemDTO> reserved = new ArrayList<>();
        try {
            // 预占库存
            items.forEach(i -> {
                stockCall("/reserve", i.getProductId(), i.getQuantity());
                reserved.add(i);
            });

            OrderDTO dto = new OrderDTO();
            dto.setOrderNo(generateOrderNo());
            dto.setUserId(req.getUserId());
            dto.setAmount(amount);
            dto.setStatus(OrderStatus.CREATED.name());
            dto.setItems(items);
            OrderDTO saved = create(req, dto);
            if (idemKey != null) {
                idemCache.put(idemKey, saved.getId());
            }
            return saved;
        } catch (RuntimeException ex) {
            // 失败补偿：释放已预占库存
            reserved.forEach(i -> safeRelease(i.getProductId(), i.getQuantity()));
            throw ex;
        }
    }

    @Override
    @Transactional
    public OrderDTO create(CreateOrderRequest req, OrderDTO computed) {
        Order order = orderConvert.toEntity(computed);
        orderMapper.insert(order);
        Long orderId = order.getId();
        if (computed.getItems() != null) {
            List<OrderItem> items = computed.getItems().stream()
                    .map(i -> orderConvert.toItemEntity(i, orderId))
                    .toList();
            items.forEach(orderItemMapper::insert);
        }
        return getById(order.getId());
    }

    @Override
    public boolean delete(Long id) {
        return orderMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return false;
        }
        order.setStatus(status);
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public boolean pay(Long id, boolean fromCallback) {
        OrderDTO order = getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        OrderStatus status = OrderStatus.valueOf(order.getStatus());
        if (!OrderStatus.canPay(status)) {
            return true; // 幂等
        }
        if (order.getItems() != null) {
            order.getItems().forEach(i -> stockCall("/deduct", i.getProductId(), i.getQuantity()));
        }
        updateStatus(id, OrderStatus.PAID.name());
        clearCart(order.getUserId(), order.getItems());
        return true;
    }

    @Override
    public boolean cancel(Long id) {
        OrderDTO order = getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        OrderStatus status = OrderStatus.valueOf(order.getStatus());
        if (!OrderStatus.canCancel(status)) {
            throw new BusinessException(400, "订单状态不允许取消");
        }
        if (order.getItems() != null) {
            order.getItems().forEach(i -> stockCall("/release", i.getProductId(), i.getQuantity()));
        }
        updateStatus(id, OrderStatus.CANCELLED.name());
        return true;
    }

    private List<OrderItemDTO> resolveItems(CreateOrderRequest req) {
        List<OrderItemDTO> raw = req.getItems();
        if (raw == null || raw.isEmpty()) {
            raw = fetchCartItems(req.getUserId());
        }
        if (raw == null || raw.isEmpty()) {
            throw new BusinessException(400, "订单项为空，请添加购物车或传入明细");
        }
        return raw.stream().map(this::enrichWithProduct).collect(Collectors.toList());
    }

    private List<OrderItemDTO> fetchCartItems(Long userId) {
        Result<List<CartItemDTO>> resp = cartApi.list(String.valueOf(userId));
        if (resp == null || resp.getCode() != 200) {
            throw new BusinessException(400, "获取购物车失败");
        }
        List<CartItemDTO> cartItems = resp.getData();
        if (cartItems == null) {
            return Collections.emptyList();
        }
        return cartItems.stream().map(c -> {
            OrderItemDTO oi = new OrderItemDTO();
            oi.setProductId(c.getProductId());
            oi.setQuantity(c.getQuantity());
            return oi;
        }).collect(Collectors.toList());
    }

    private OrderItemDTO enrichWithProduct(OrderItemDTO item) {
        Result<ProductDTO> resp = productApi.get(item.getProductId());
        if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
            throw new BusinessException(400, "商品不存在: " + item.getProductId());
        }
        ProductDTO p = resp.getData();
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
        Result<Boolean> resp = switch (path) {
            case "/reserve" -> stockApi.reserve(dto);
            case "/release" -> stockApi.release(dto);
            case "/deduct" -> stockApi.deduct(dto);
            default -> throw new BusinessException(400, "非法库存操作");
        };
        if (resp == null || resp.getCode() != 200 || Boolean.FALSE.equals(resp.getData())) {
            throw new BusinessException(400, "库存操作失败: " + path);
        }
    }

    private void safeRelease(Long productId, Integer qty) {
        try {
            stockCall("/release", productId, qty);
        } catch (Exception ignored) {
        }
    }

    private void clearCart(Long userId, List<OrderItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        items.forEach(i -> cartApi.remove(String.valueOf(userId), i.getProductId()));
    }

    private String generateOrderNo() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = orderConvert.toDTO(order);
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        dto.setItems(orderConvert.toItemDTOs(items));
        return dto;
    }
}

