package com.example.cart.web.client;

import com.example.common.dto.Result;
import com.example.product.api.dto.ProductRecDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", url = "${service.product.base-url:http://localhost:8085}")
public interface ProductRecommendClient {

    @GetMapping("/product/rec/by-product")
    Result<List<ProductRecDTO>> recommendByProduct(@RequestParam("productId") Long productId,
                                                   @RequestParam(value = "limit", defaultValue = "6") Integer limit);
}
















