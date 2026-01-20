package com.example.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "推荐商品返回")
public class ProductRecDTO {
    private Long productId;
    private String reason;
    private Double score;
}






















