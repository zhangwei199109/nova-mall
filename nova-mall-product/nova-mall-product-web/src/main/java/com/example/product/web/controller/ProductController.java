package com.example.product.web.controller;

import com.example.common.dto.Result;
import com.example.product.api.ProductApi;
import com.example.product.api.dto.ProductDTO;
import com.example.product.api.dto.ProductRecDTO;
import com.example.product.service.ProductAppService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController implements ProductApi {

    private final ProductAppService productAppService;

    public ProductController(ProductAppService productAppService) {
        this.productAppService = productAppService;
    }

    @Override
    public Result<List<ProductDTO>> list() {
        return Result.success(productAppService.listProducts());
    }

    @Override
    public Result<ProductDTO> get(Long id) {
        return Result.success(productAppService.getById(id));
    }

    @Override
    public Result<ProductDTO> create(@Valid ProductDTO dto) {
        return Result.success(productAppService.create(dto));
    }

    @Override
    public Result<ProductDTO> update(Long id, @Valid ProductDTO dto) {
        dto.setId(id);
        return Result.success(productAppService.update(dto));
    }

    @Override
    public Result<Boolean> delete(Long id) {
        return Result.success(productAppService.delete(id));
    }

    @Override
    public Result<List<ProductRecDTO>> recommendByProduct(Long productId, Integer limit) {
        return Result.success(productAppService.recommendByProduct(productId, limit));
    }
}



