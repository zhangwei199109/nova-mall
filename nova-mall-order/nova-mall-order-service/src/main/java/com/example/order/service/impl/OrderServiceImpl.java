package com.example.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.PageParam;
import com.example.common.dto.PageResult;
import com.example.common.exception.BusinessException;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.api.dto.OrderItemDTO;
import com.example.order.service.OrderAppService;
import com.example.order.service.config.SnowflakeIdGenerator;
import com.example.order.service.entity.Order;
import com.example.order.service.entity.OrderItem;
import com.example.order.service.entity.OrderCallbackLog;
import com.example.order.service.enums.OrderStatus;
import com.example.order.service.mapper.OrderCallbackLogMapper;
import com.example.order.service.mapper.OrderItemMapper;
import com.example.order.service.mapper.OrderMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderAppService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final OrderCallbackLogMapper orderCallbackLogMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            SnowflakeIdGenerator snowflakeIdGenerator,
                            OrderCallbackLogMapper orderCallbackLogMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.orderCallbackLogMapper = orderCallbackLogMapper;
    }

    @Override
    public PageResult<OrderDTO> list(Long userId, PageParam pageParam) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        PageParam normalized = (pageParam == null ? new PageParam() : pageParam).normalized(1, 20, 100);
        Page<Order> mpPage = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        Page<Order> result = orderMapper.selectPage(mpPage, new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getId));
        List<OrderDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public OrderDTO getById(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new BusinessException(400, "参数不能为空");
        }
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getUserId, userId));
        return order == null ? null : toDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO createOrder(String idemKey, CreateOrderRequest req) {
        if (req == null || req.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        List<OrderItemDTO> items = req.getItems();
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "订单项不能为空");
        }
        validateItems(items);
        if (StringUtils.hasText(idemKey)) {
            Order existed = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, req.getUserId())
                    .eq(Order::getIdemKey, idemKey));
            if (existed != null) {
                return toDTO(existed);
            }
        }
        OrderDTO computed = new OrderDTO();
        computed.setUserId(req.getUserId());
        computed.setOrderNo(generateOrderNo());
        computed.setStatus(OrderStatus.CREATED.name());
        computed.setItems(items);
        computed.setAmount(calcAmount(items));
        try {
            return create(req, computed, idemKey);
        } catch (DuplicateKeyException e) {
            if (StringUtils.hasText(idemKey)) {
                Order existed = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, req.getUserId())
                        .eq(Order::getIdemKey, idemKey));
                if (existed != null) {
                    return toDTO(existed);
                }
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderDTO create(CreateOrderRequest req, OrderDTO computed, String idemKey) {
        Order order = toEntity(computed);
        if (StringUtils.hasText(idemKey)) {
            order.setIdemKey(idemKey);
        }
        orderMapper.insert(order);
        Long orderId = order.getId();
        if (computed.getItems() != null) {
            List<OrderItem> items = computed.getItems().stream()
                    .map(i -> toItemEntity(orderId, i))
                    .toList();
            items.forEach(orderItemMapper::insert);
        }
        return getById(order.getId(), order.getUserId());
    }

    @Override
    public boolean delete(Long id) {
        return orderMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        OrderStatus target = parseStatus(status).orElseThrow(() -> new BusinessException(400, "状态非法"));
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return false;
        }
        return orderMapper.update(new Order(), new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getVersion, order.getVersion())
                .set(Order::getStatus, target.name())
                .set(Order::getVersion, order.getVersion() + 1)) > 0;
    }

    @Override
    @Transactional
    public boolean pay(Long id, boolean fromCallback) {
        return pay(id, fromCallback, null);
    }

    @Override
    @Transactional
    public boolean pay(Long id, boolean fromCallback, String callbackKey) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        OrderStatus current = parseStatus(order.getStatus())
                .orElseThrow(() -> new BusinessException(400, "订单状态非法"));
        if (current == OrderStatus.PAID) {
            return true; // 幂等：已支付视为成功
        }
        if (!OrderStatus.canPay(current)) {
            throw new BusinessException(400, "当前状态不可支付");
        }
        if (fromCallback && StringUtils.hasText(callbackKey)) {
            try {
                OrderCallbackLog log = new OrderCallbackLog();
                log.setOrderId(id);
                log.setCallbackKey(callbackKey);
                orderCallbackLogMapper.insert(log);
            } catch (DuplicateKeyException e) {
                return true; // 回调幂等：重复回调直接视为成功
            }
        }
        int updated = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getStatus, OrderStatus.CREATED.name())
                .eq(Order::getVersion, order.getVersion())
                .set(Order::getStatus, OrderStatus.PAID.name())
                .set(Order::getVersion, order.getVersion() + 1));
        if (updated == 0) {
            throw new BusinessException(409, "支付冲突，请重试");
        }
        return true;
    }

    @Override
    @Transactional
    public boolean cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        OrderStatus current = parseStatus(order.getStatus())
                .orElseThrow(() -> new BusinessException(400, "订单状态非法"));
        if (current == OrderStatus.CANCELLED) {
            return true; // 幂等：已取消视为成功
        }
        if (!OrderStatus.canCancel(current)) {
            throw new BusinessException(400, "当前状态不可取消");
        }
        int updated = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getStatus, OrderStatus.CREATED.name())
                .eq(Order::getVersion, order.getVersion())
                .set(Order::getStatus, OrderStatus.CANCELLED.name())
                .set(Order::getVersion, order.getVersion() + 1));
        if (updated == 0) {
            throw new BusinessException(409, "取消冲突，请重试");
        }
        return true;
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setAmount(order.getAmount());
        dto.setStatus(order.getStatus());
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    private Order toEntity(OrderDTO dto) {
        Order order = new Order();
        order.setOrderNo(dto.getOrderNo());
        order.setUserId(dto.getUserId());
        order.setAmount(dto.getAmount());
        order.setStatus(dto.getStatus());
        return order;
    }

    private OrderItem toItemEntity(Long orderId, OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(dto.getProductId());
        item.setProductName(dto.getProductName());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        return item;
    }

    private OrderItemDTO toItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        return dto;
    }

    private java.util.Optional<OrderStatus> parseStatus(String status) {
        try {
            return java.util.Optional.of(OrderStatus.valueOf(status));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    private BigDecimal calcAmount(List<OrderItemDTO> items) {
        return items.stream()
                .map(i -> {
                    BigDecimal price = i.getPrice() == null ? BigDecimal.ZERO : i.getPrice();
                    int quantity = i.getQuantity() == null ? 0 : i.getQuantity();
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateItems(List<OrderItemDTO> items) {
        for (OrderItemDTO i : items) {
            if (i.getProductId() == null) {
                throw new BusinessException(400, "商品ID不能为空");
            }
            if (i.getQuantity() == null || i.getQuantity() < 1) {
                throw new BusinessException(400, "数量至少为1");
            }
            if (i.getPrice() == null || i.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(400, "价格必须大于等于0");
            }
        }
    }

    private String generateOrderNo() {
        return "ORD-" + snowflakeIdGenerator.nextIdStr();
    }
}

