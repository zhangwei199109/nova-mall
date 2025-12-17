package com.example.order.web.controller;

import com.example.common.dto.Result;
import com.example.order.api.OrderApi;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import com.example.order.web.convert.OrderWebConvert;
import com.example.order.service.OrderAppService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class OrderController implements OrderApi {

    private final OrderAppService orderService;
    private final OrderWebConvert orderWebConvert;

    public OrderController(OrderAppService orderService, OrderWebConvert orderWebConvert) {
        this.orderService = orderService;
        this.orderWebConvert = orderWebConvert;
    }

    @Override
    public Result<List<OrderDTO>> list() {
        return Result.success(orderService.list());
    }

    @Override
    public Result<OrderDTO> detail(@PathVariable Long id) {
        OrderDTO dto = orderService.getById(id);
        if (dto == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(dto);
    }

    @Override
    public Result<OrderDTO> create(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                   @Valid @RequestBody CreateOrderRequest req) {
        return Result.success(orderService.createOrder(idemKey, orderWebConvert.toCreateRequest(req)));
    }

    @Override
    public Result<Boolean> pay(@PathVariable Long id) {
        return Result.success(orderService.pay(id, false));
    }

    @Override
    public Result<Boolean> payCallback(@PathVariable Long id) {
        return Result.success(orderService.pay(id, true));
    }

    @Override
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(orderService.cancel(id));
    }

    @Override
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = orderService.delete(id);
        if (!ok) {
            return Result.error(404, "订单不存在");
        }
        return Result.success();
    }
}


