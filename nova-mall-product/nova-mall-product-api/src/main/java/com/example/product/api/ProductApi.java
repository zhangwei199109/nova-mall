package com.example.product.api;

import com.example.common.dto.Result;
import com.example.product.api.dto.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品接口")
@RequestMapping("/product")
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
}



