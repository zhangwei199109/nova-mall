package com.example.order.service.impl;

import com.example.api.dto.OrderDTO;
import com.example.order.entity.Order;
import com.example.order.mapper.OrderMapper;
import com.example.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

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
    public OrderDTO create(OrderDTO dto) {
        Order order = toEntity(dto);
        orderMapper.insert(order);
        return toDTO(order);
    }

    @Override
    public OrderDTO update(Long id, OrderDTO dto) {
        Order exist = orderMapper.selectById(id);
        if (exist == null) {
            return null;
        }
        Order order = toEntity(dto);
        order.setId(id);
        orderMapper.updateById(order);
        return toDTO(order);
    }

    @Override
    public boolean delete(Long id) {
        return orderMapper.deleteById(id) > 0;
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setAmount(order.getAmount());
        dto.setStatus(order.getStatus());
        return dto;
    }

    private Order toEntity(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setOrderNo(dto.getOrderNo());
        order.setUserId(dto.getUserId());
        order.setAmount(dto.getAmount());
        order.setStatus(dto.getStatus());
        return order;
    }
}



