package com.example.product.service;

import com.example.product.api.dto.ProductDTO;

import java.util.List;

/**
 * 产品领域对外（RPC）接口。
 */
public interface ProductAppService {
    List<ProductDTO> listProducts();

    ProductDTO getById(Long id);

    ProductDTO create(ProductDTO dto);

    ProductDTO update(ProductDTO dto);

    boolean delete(Long id);
}









