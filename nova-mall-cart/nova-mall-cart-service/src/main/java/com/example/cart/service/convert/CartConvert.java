package com.example.cart.service.convert;

import com.example.cart.api.dto.CartItemDTO;
import com.example.cart.service.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CartConvert {

    CartItemDTO toDTO(CartItem entity);

    CartItem toEntity(CartItemDTO dto);

    void updateEntity(CartItemDTO dto, @MappingTarget CartItem entity);
}

