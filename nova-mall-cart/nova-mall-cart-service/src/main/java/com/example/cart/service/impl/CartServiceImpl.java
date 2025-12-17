package com.example.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cart.api.dto.CartItemDTO;
import com.example.cart.service.CartAppService;
import com.example.cart.service.convert.CartConvert;
import com.example.cart.service.entity.CartItem;
import com.example.cart.service.mapper.CartItemMapper;
import com.example.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartAppService {

    private final CartItemMapper cartItemMapper;
    private final CartConvert cartConvert;

    public CartServiceImpl(CartItemMapper cartItemMapper, CartConvert cartConvert) {
        this.cartItemMapper = cartItemMapper;
        this.cartConvert = cartConvert;
    }

    @Override
    public List<CartItemDTO> list(String userId) {
        String uid = normalizeUser(userId);
        List<CartItem> list = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, uid));
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public CartItemDTO add(String userId, CartItemDTO item) {
        String uid = normalizeUser(userId);
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, uid)
                .eq(CartItem::getProductId, item.getProductId()));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
            cartItemMapper.updateById(existing);
            return toDTO(existing);
        }
        CartItem entity = cartConvert.toEntity(item);
        entity.setUserId(uid);
        cartItemMapper.insert(entity);
        return toDTO(cartItemMapper.selectById(entity.getId()));
    }

    @Override
    public CartItemDTO updateQuantity(String userId, Long productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException(400, "数量必须大于0");
        }
        String uid = normalizeUser(userId);
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, uid)
                .eq(CartItem::getProductId, productId));
        if (existing == null) {
            throw new BusinessException(404, "购物车中不存在该商品");
        }
        existing.setQuantity(quantity);
        cartItemMapper.updateById(existing);
        return toDTO(existing);
    }

    @Override
    public boolean remove(String userId, Long productId) {
        String uid = normalizeUser(userId);
        return cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, uid)
                .eq(CartItem::getProductId, productId)) > 0;
    }

    @Override
    public boolean clear(String userId) {
        String uid = normalizeUser(userId);
        List<CartItem> list = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, uid));
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        return cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, uid)) > 0;
    }

    private CartItemDTO toDTO(CartItem item) {
        return cartConvert.toDTO(item);
    }

    private String normalizeUser(String userId) {
        return (userId == null || userId.isBlank()) ? "guest" : userId;
    }
}



