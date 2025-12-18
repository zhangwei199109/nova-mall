package com.example.order.web.controller;

import com.example.common.dto.PageParam;
import com.example.common.dto.PageResult;
import com.example.common.dto.Result;
import com.example.common.exception.BusinessException;
import com.example.common.web.AuthContext;
import com.example.order.api.OrderApi;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.service.OrderAppService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrderApi {

    private final OrderAppService orderAppService;
    private final AuthContext authContext;

    public OrderController(OrderAppService orderAppService,
                           AuthContext authContext) {
        this.orderAppService = orderAppService;
        this.authContext = authContext;
    }

    @Override
    public Result<PageResult<OrderDTO>> list(PageParam pageParam) {
        Long userId = authContext.currentUserId();
        return Result.success(orderAppService.list(userId, pageParam));
    }

    @Override
    public Result<OrderDTO> detail(Long id) {
        Long userId = authContext.currentUserId();
        OrderDTO dto = orderAppService.getById(id, userId);
        if (dto == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return Result.success(dto);
    }

    @Override
    public Result<OrderDTO> create(String idemKey, @Valid CreateOrderRequest req) {
        return Result.success(orderAppService.createOrder(idemKey, req));
    }

    @Override
    public Result<Boolean> pay(Long id) {
        return Result.success(orderAppService.pay(id, false));
    }

    @Override
    public Result<Boolean> payCallback(Long id, String callbackKey) {
        return Result.success(orderAppService.pay(id, true, callbackKey));
    }

    @Override
    public Result<Boolean> cancel(Long id) {
        return Result.success(orderAppService.cancel(id));
    }

    @Override
    public Result<Void> delete(Long id) {
        boolean ok = orderAppService.delete(id);
        if (!ok) {
            throw new BusinessException(404, "订单不存在或已删除");
        }
        return Result.success();
    }
}
