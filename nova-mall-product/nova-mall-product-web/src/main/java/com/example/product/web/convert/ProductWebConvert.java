package com.example.product.web.convert;

import com.example.product.api.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductWebConvert {

    ProductDTO toCreateDto(ProductDTO dto);

    @Mapping(target = "id", source = "id")
    ProductDTO toUpdateDto(Long id, ProductDTO dto);
}

