package com.example.cart.web.controller;

import com.example.cart.api.CartApi;
import com.example.cart.service.CartAppService;
import com.example.cart.api.dto.CartItemDTO;
import com.example.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CartController implements CartApi {

    private final CartAppService cartAppService;

    public CartController(CartAppService cartAppService) {
        this.cartAppService = cartAppService;
    }

    @Override
    public Result<List<CartItemDTO>> list(String userId) {
        return Result.success(cartAppService.list(userId));
    }

    @Override
    public Result<CartItemDTO> add(String userId, @Valid CartItemDTO item) {
        return Result.success(cartAppService.add(userId, item));
    }

    @Override
    public Result<CartItemDTO> update(String userId, Long productId, Integer quantity) {
        return Result.success(cartAppService.updateQuantity(userId, productId, quantity));
    }

    @Override
    public Result<Boolean> remove(String userId, Long productId) {
        return Result.success(cartAppService.remove(userId, productId));
    }

    @Override
    public Result<Boolean> clear(String userId) {
        return Result.success(cartAppService.clear(userId));
    }
}



