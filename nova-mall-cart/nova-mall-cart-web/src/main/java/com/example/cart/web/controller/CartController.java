package com.example.cart.web.controller;

import com.example.cart.api.CartApi;
import com.example.cart.api.dto.CartItemDTO;
import com.example.cart.service.CartAppService;
import com.example.common.dto.Result;
import com.example.common.web.AuthContext;
import com.example.cart.web.client.ProductRecommendClient;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController implements CartApi {

    private final CartAppService cartAppService;
    private final AuthContext authContext;
    private final ProductRecommendClient productRecommendClient;

    public CartController(CartAppService cartAppService, AuthContext authContext,
                          ProductRecommendClient productRecommendClient) {
        this.cartAppService = cartAppService;
        this.authContext = authContext;
        this.productRecommendClient = productRecommendClient;
    }

    @Override
    public Result<java.util.List<CartItemDTO>> list() {
        String userId = String.valueOf(authContext.currentUserId());
        java.util.List<CartItemDTO> items = cartAppService.list(userId);
        // 示例：基于第一件商品做推荐，后续可扩展为更多策略
        if (!items.isEmpty()) {
            Long productId = items.get(0).getProductId();
            // 推荐结果暂未合并到返回体，后续可扩展返回或透传至前端
            productRecommendClient.recommendByProduct(productId, 6);
        }
        return Result.success(items);
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



