package com.example.product.service.convert;

import com.example.product.api.dto.ProductDTO;
import com.example.product.service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductConvert {

    ProductDTO toDTO(Product entity);

    Product toEntity(ProductDTO dto);

    void updateEntity(ProductDTO dto, @MappingTarget Product entity);
}

