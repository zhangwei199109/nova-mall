package com.example.cart.api;

import com.example.cart.api.dto.CartItemDTO;
import com.example.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车接口")
@RequestMapping("/cart")
@FeignClient(name = "cart-service", url = "${service.cart.base-url:http://localhost:8086}")
public interface CartApi {

    @Operation(summary = "查看购物车")
    @GetMapping
    Result<List<CartItemDTO>> list(@RequestHeader(value = "X-User-Id", required = false) String userId);

    @Operation(summary = "添加商品到购物车")
    @PostMapping
    Result<CartItemDTO> add(@RequestHeader(value = "X-User-Id", required = false) String userId,
                            @Valid @RequestBody CartItemDTO item);

    @Operation(summary = "更新购物车数量")
    @PutMapping("/{productId}")
    Result<CartItemDTO> update(@RequestHeader(value = "X-User-Id", required = false) String userId,
                               @PathVariable("productId") Long productId,
                               @RequestParam("quantity") Integer quantity);

    @Operation(summary = "移除购物车商品")
    @DeleteMapping("/{productId}")
    Result<Boolean> remove(@RequestHeader(value = "X-User-Id", required = false) String userId,
                           @PathVariable("productId") Long productId);

    @Operation(summary = "清空购物车")
    @DeleteMapping
    Result<Boolean> clear(@RequestHeader(value = "X-User-Id", required = false) String userId);
}



