package com.example.order.service;

import com.example.common.dto.PageParam;
import com.example.common.dto.PageResult;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;

public interface OrderAppService {

    PageResult<OrderDTO> list(Long userId, PageParam pageParam);

    OrderDTO getById(Long id, Long userId);

    OrderDTO createOrder(String idemKey, CreateOrderRequest req);

    OrderDTO create(CreateOrderRequest req, OrderDTO computed, String idemKey);

    boolean delete(Long id);

    boolean updateStatus(Long id, String status);

    boolean pay(Long id, boolean fromCallback);

    boolean pay(Long id, boolean fromCallback, String callbackKey);

    boolean cancel(Long id);
}
