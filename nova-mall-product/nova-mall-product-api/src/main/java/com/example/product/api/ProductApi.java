package com.example.product.api;

import com.example.common.dto.Result;
import com.example.product.api.dto.ProductDTO;
import com.example.product.api.dto.ProductRecDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "product-service",
        url = "${service.product.base-url:http://localhost:8085}",
        path = "/product")
@Tag(name = "商品接口")
public interface ProductApi {

    @Operation(summary = "商品列表")
    @GetMapping
    Result<List<ProductDTO>> list();

    @Operation(summary = "查询商品")
    @GetMapping("/{id}")
    Result<ProductDTO> get(@PathVariable("id") Long id);

    @Operation(summary = "创建商品")
    @PostMapping
    Result<ProductDTO> create(@Valid @RequestBody ProductDTO dto);

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    Result<ProductDTO> update(@PathVariable("id") Long id, @Valid @RequestBody ProductDTO dto);

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    Result<Boolean> delete(@PathVariable("id") Long id);

    @Operation(summary = "根据商品推荐（简单规则版）")
    @GetMapping("/rec/by-product")
    Result<List<ProductRecDTO>> recommendByProduct(@RequestParam("productId") Long productId,
                                                   @RequestParam(value = "limit", defaultValue = "6") Integer limit);
}



