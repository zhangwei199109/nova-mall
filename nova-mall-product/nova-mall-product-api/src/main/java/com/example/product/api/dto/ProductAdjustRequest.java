package com.example.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "商品销量/库存调整请求（内部调用）")
public class ProductAdjustRequest {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long productId;

    @Schema(description = "数量（正整数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull
    @Min(1)
    private Integer quantity;
}


