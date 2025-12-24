package com.example.product.service;

import com.example.product.api.dto.ProductDTO;
import com.example.product.api.dto.ProductRecDTO;

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

    /**
     * 简单按商品推荐，规则/热度驱动。
     */
    java.util.List<ProductRecDTO> recommendByProduct(Long productId, Integer limit);
}









