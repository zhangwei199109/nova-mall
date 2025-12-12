package com.example.cart.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cart.service.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}



