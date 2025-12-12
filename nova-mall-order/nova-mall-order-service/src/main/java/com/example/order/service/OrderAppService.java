package com.example.order.service;

import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;

import java.util.List;

public interface OrderAppService {

    List<OrderDTO> list();

    OrderDTO getById(Long id);

    /**
     * 应用层创建订单（含商品校验、预占库存、购物车获取）。
     */
    OrderDTO createOrder(String idemKey, CreateOrderRequest req);

    /**
     * 底层持久化创建（已计算金额/状态的订单），供应用层内部使用。
     */
    OrderDTO create(CreateOrderRequest req, OrderDTO computed);

    boolean updateStatus(Long id, String status);

    boolean delete(Long id);

    boolean pay(Long id, boolean fromCallback);

    boolean cancel(Long id);
}





