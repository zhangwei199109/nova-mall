package com.example.cart.service;

import com.example.cart.api.dto.CartItemDTO;

import java.util.List;

public interface CartAppService {

    List<CartItemDTO> list(String userId);

    CartItemDTO add(String userId, CartItemDTO item);

    CartItemDTO updateQuantity(String userId, Long productId, Integer quantity);

    boolean remove(String userId, Long productId);

    boolean clear(String userId);
}




















