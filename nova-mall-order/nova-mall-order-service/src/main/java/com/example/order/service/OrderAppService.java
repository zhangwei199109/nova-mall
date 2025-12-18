package com.example.order.service;

import com.example.common.dto.PageResult;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;

public interface OrderAppService {

    PageResult<OrderDTO> list(Long userId, com.example.common.dto.PageParam pageParam);

    OrderDTO getById(Long id, Long userId);

    /**
     * 应用层创建订单（含商品校验、预占库存、购物车获取）。
     */
    OrderDTO createOrder(String idemKey, CreateOrderRequest req);

    /**
     * 底层持久化创建（已计算金额/状态的订单），供应用层内部使用。
     * @param idemKey 幂等键，可为空
     */
    OrderDTO create(CreateOrderRequest req, OrderDTO computed, String idemKey);

    boolean updateStatus(Long id, String status);

    boolean delete(Long id);

    boolean pay(Long id, boolean fromCallback);

    boolean pay(Long id, boolean fromCallback, String callbackKey);

    boolean cancel(Long id);
}





