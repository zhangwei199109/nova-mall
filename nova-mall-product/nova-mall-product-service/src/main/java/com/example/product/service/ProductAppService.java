package com.example.product.service;

import com.example.common.dto.PageParam;
import com.example.common.dto.PageResult;
import com.example.product.api.dto.ProductDTO;
import com.example.product.api.dto.ProductRecDTO;
import com.example.product.api.dto.ProductQuery;

import java.util.List;

/**
 * 产品领域对外（RPC）接口。
 */
public interface ProductAppService {
    List<ProductDTO> listProducts();

    PageResult<ProductDTO> page(PageParam pageParam, ProductQuery query);

    ProductDTO getById(Long id);

    ProductDTO create(ProductDTO dto);

    ProductDTO update(ProductDTO dto);

    boolean delete(Long id);

    /**
     * 热销商品TOP榜（按销量）。
     */
    List<ProductDTO> topBySales(Integer limit);

    /**
     * 简单按商品推荐，规则/热度驱动。
     */
    java.util.List<ProductRecDTO> recommendByProduct(Long productId, Integer limit);

    boolean onShelf(Long id);

    boolean offShelf(Long id);

    /**
     * 支付后扣减库存并累加销量（内部调用）。
     */
    boolean adjustAfterPay(java.util.List<com.example.product.api.dto.ProductAdjustRequest> items);
}









