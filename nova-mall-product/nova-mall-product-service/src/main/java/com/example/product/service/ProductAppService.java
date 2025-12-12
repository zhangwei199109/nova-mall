package com.example.product.service;

import com.example.product.api.dto.ProductDTO;

import java.util.List;

public interface ProductAppService {
    List<ProductDTO> listProducts();

    ProductDTO getById(Long id);

    ProductDTO create(ProductDTO dto);

    ProductDTO update(ProductDTO dto);

    boolean delete(Long id);
}




