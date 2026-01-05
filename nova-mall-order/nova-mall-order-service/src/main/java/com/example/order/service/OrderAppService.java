package com.example.order.service;

import com.example.common.dto.PageParam;
import com.example.common.dto.PageResult;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.api.dto.OrderQuery;
import com.example.order.api.dto.OrderStatusUpdateRequest;

public interface OrderAppService {

    PageResult<OrderDTO> list(Long userId, PageParam pageParam, OrderQuery query);

    OrderDTO getById(Long id, Long userId);

    OrderDTO createOrder(String idemKey, CreateOrderRequest req);

    OrderDTO create(CreateOrderRequest req, OrderDTO computed, String idemKey);

    boolean delete(Long id, Long userId);

    boolean updateStatus(Long id, String status);

    boolean updateStatusInternal(Long id, OrderStatusUpdateRequest req);

    boolean pay(Long id, Long userId, boolean fromCallback);

    boolean pay(Long id, Long userId, boolean fromCallback, String callbackKey);

    boolean cancel(Long id, Long userId);
}
