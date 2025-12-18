package com.example.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.order.api.OrderAppService;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.api.dto.OrderItemDTO;
import com.example.order.service.entity.Order;
import com.example.order.service.entity.OrderItem;
import com.example.order.service.enums.OrderStatus;
import com.example.order.service.mapper.OrderItemMapper;
import com.example.order.service.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderAppService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
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
    @Transactional
    public OrderDTO create(CreateOrderRequest req, OrderDTO computed) {
        Order order = toEntity(computed);
        orderMapper.insert(order);
        Long orderId = order.getId();
        if (computed.getItems() != null) {
            List<OrderItem> items = computed.getItems().stream()
                    .map(i -> toItemEntity(orderId, i))
                    .collect(Collectors.toList());
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
}

