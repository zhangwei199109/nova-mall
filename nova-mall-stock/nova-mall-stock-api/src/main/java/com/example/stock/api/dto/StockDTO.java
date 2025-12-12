package com.example.stock.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "库存信息")
public class StockDTO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "可用库存")
    @Min(value = 0, message = "可用库存不能为负")
    private Integer available;

    @Schema(description = "锁定库存")
    @Min(value = 0, message = "锁定库存不能为负")
    private Integer locked;
}



