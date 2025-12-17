package com.example.product.web.controller;

import com.example.common.dto.Result;
import com.example.product.api.ProductApi;
import com.example.product.service.ProductAppService;
import com.example.product.api.dto.ProductDTO;
import com.example.product.web.convert.ProductWebConvert;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController implements ProductApi {

    private final ProductAppService productAppService;
    private final ProductWebConvert productWebConvert;

    public ProductController(ProductAppService productAppService, ProductWebConvert productWebConvert) {
        this.productAppService = productAppService;
        this.productWebConvert = productWebConvert;
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
        return Result.success(productAppService.create(productWebConvert.toCreateDto(dto)));
    }

    @Override
    public Result<ProductDTO> update(Long id, @Valid ProductDTO dto) {
        return Result.success(productAppService.update(productWebConvert.toUpdateDto(id, dto)));
    }

    @Override
    public Result<Boolean> delete(Long id) {
        return Result.success(productAppService.delete(id));
    }
}



