package com.example.order.service;

import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;

import java.util.List;

public interface OrderAppService {

    List<OrderDTO> list();

    OrderDTO getById(Long id);

    OrderDTO create(CreateOrderRequest req, OrderDTO computed);

    boolean updateStatus(Long id, String status);

    boolean delete(Long id);
}




