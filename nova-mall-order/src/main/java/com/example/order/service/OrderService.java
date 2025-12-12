package com.example.order.service;

import com.example.api.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    List<OrderDTO> list();

    OrderDTO getById(Long id);

    OrderDTO create(OrderDTO dto);

    OrderDTO update(Long id, OrderDTO dto);

    boolean delete(Long id);
}



