package com.example.cart.web.convert;

import com.example.cart.api.dto.CartItemDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartWebConvert {

    CartItemDTO toCreateDto(CartItemDTO dto);
}

