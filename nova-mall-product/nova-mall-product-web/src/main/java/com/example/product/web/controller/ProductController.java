package com.example.product.web.controller;

import com.example.common.dto.PageParam;
import com.example.common.dto.PageResult;
import com.example.common.dto.Result;
import com.example.product.api.ProductApi;
import com.example.product.api.dto.ProductDTO;
import com.example.product.api.dto.ProductRecDTO;
import com.example.product.api.dto.ProductQuery;
import com.example.product.api.dto.ProductAdjustRequest;
import com.example.product.service.ProductAppService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
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
    public Result<PageResult<ProductDTO>> page(@ModelAttribute PageParam pageParam,
                                               @ModelAttribute ProductQuery query) {
        return Result.success(productAppService.page(pageParam, query));
    }

    @Override
    public Result<List<ProductDTO>> top(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return Result.success(productAppService.topBySales(limit));
    }

    @Override
    public Result<ProductDTO> get(Long id) {
        return Result.success(productAppService.getById(id));
    }

    @Override
    public Result<ProductDTO> create(@Valid @RequestBody ProductDTO dto) {
        return Result.success(productAppService.create(dto));
    }

    @Override
    public Result<ProductDTO> update(@PathVariable("id") Long id, @Valid @RequestBody ProductDTO dto) {
        dto.setId(id);
        return Result.success(productAppService.update(dto));
    }

    @Override
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        return Result.success(productAppService.delete(id));
    }

    @Override
    public Result<Boolean> onShelf(@PathVariable("id") Long id) {
        return Result.success(productAppService.onShelf(id));
    }

    @Override
    public Result<Boolean> offShelf(@PathVariable("id") Long id) {
        return Result.success(productAppService.offShelf(id));
    }

    @Override
    public Result<List<ProductRecDTO>> recommendByProduct(@RequestParam("productId") Long productId,
                                                          @RequestParam(value = "limit", defaultValue = "6") Integer limit) {
        return Result.success(productAppService.recommendByProduct(productId, limit));
    }

    @Override
    public Result<Boolean> adjustAfterPay(@Valid @RequestBody List<ProductAdjustRequest> items) {
        return Result.success(productAppService.adjustAfterPay(items));
    }
}



