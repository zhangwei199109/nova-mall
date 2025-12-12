package com.example.order.controller;

import com.example.api.dto.OrderDTO;
import com.example.api.dto.Result;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单服务", description = "订单基础 CRUD 接口（独立微服务）")
@RestController
@RequestMapping("/order")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public Result<List<OrderDTO>> list() {
        return Result.success(orderService.list());
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderDTO> detail(@PathVariable Long id) {
        OrderDTO dto = orderService.getById(id);
        if (dto == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(dto);
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderDTO> create(@Valid @RequestBody OrderDTO dto) {
        return Result.success(orderService.create(dto));
    }

    @Operation(summary = "更新订单")
    @PutMapping("/{id}")
    public Result<OrderDTO> update(@PathVariable Long id, @Valid @RequestBody OrderDTO dto) {
        OrderDTO updated = orderService.update(id, dto);
        if (updated == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(updated);
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = orderService.delete(id);
        if (!ok) {
            return Result.error(404, "订单不存在");
        }
        return Result.success();
    }
}



