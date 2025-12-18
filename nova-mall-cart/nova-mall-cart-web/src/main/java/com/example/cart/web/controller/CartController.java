package com.example.cart.web.controller;

import com.example.cart.api.CartApi;
import com.example.cart.api.dto.CartItemDTO;
import com.example.cart.service.CartAppService;
import com.example.common.dto.Result;
import com.example.common.web.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController implements CartApi {

    private final CartAppService cartAppService;
    private final AuthContext authContext;

    public CartController(CartAppService cartAppService, AuthContext authContext) {
        this.cartAppService = cartAppService;
        this.authContext = authContext;
    }

    @Override
    public Result<java.util.List<CartItemDTO>> list() {
        String userId = String.valueOf(authContext.currentUserId());
        return Result.success(cartAppService.list(userId));
    }

    @Override
    public Result<CartItemDTO> add(@Valid CartItemDTO item) {
        String userId = String.valueOf(authContext.currentUserId());
        return Result.success(cartAppService.add(userId, item));
    }

    @Override
    public Result<CartItemDTO> update(Long productId, Integer quantity) {
        String userId = String.valueOf(authContext.currentUserId());
        return Result.success(cartAppService.updateQuantity(userId, productId, quantity));
    }

    @Override
    public Result<Boolean> remove(Long productId) {
        String userId = String.valueOf(authContext.currentUserId());
        return Result.success(cartAppService.remove(userId, productId));
    }

    @Override
    public Result<Boolean> clear() {
        String userId = String.valueOf(authContext.currentUserId());
        return Result.success(cartAppService.clear(userId));
    }
}



