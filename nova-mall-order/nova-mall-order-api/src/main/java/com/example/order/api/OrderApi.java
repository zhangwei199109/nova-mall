package com.example.order.api;

import com.example.common.dto.PageResult;
import com.example.common.dto.Result;
import com.example.order.api.dto.CreateOrderRequest;
import com.example.order.api.dto.OrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单服务", description = "订单对外 HTTP 契约")
@RequestMapping("/order")
public interface OrderApi {

    @Operation(summary = "订单列表（按当前用户分页）")
    @GetMapping("/list")
    Result<PageResult<OrderDTO>> list(@RequestParam(defaultValue = "1") Integer pageNo,
                                      @RequestParam(defaultValue = "20") Integer pageSize);

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    Result<OrderDTO> detail(@PathVariable Long id);

    @Operation(summary = "创建订单（预占库存）")
    @PostMapping
    Result<OrderDTO> create(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                            @Valid @RequestBody CreateOrderRequest req);

    @Operation(summary = "支付订单（扣减锁定库存）")
    @PostMapping("/{id}/pay")
    Result<Boolean> pay(@PathVariable Long id);

    @Operation(summary = "支付回调（模拟异步）")
    @PostMapping("/{id}/pay/callback")
    Result<Boolean> payCallback(@PathVariable Long id,
                                @RequestHeader(value = "Idempotency-Key", required = false) String callbackKey);

    @Operation(summary = "取消订单（释放锁定库存）")
    @PostMapping("/{id}/cancel")
    Result<Boolean> cancel(@PathVariable Long id);

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    Result<Void> delete(@PathVariable Long id);
}

