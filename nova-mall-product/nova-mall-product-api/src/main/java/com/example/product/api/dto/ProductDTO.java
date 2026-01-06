package com.example.product.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品信息")
public class ProductDTO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.99")
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @JsonProperty("price")
    private BigDecimal price;

    @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负")
    private Integer stock;

    @Schema(description = "类目ID")
    private Long categoryId;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "标签，逗号分隔")
    private String tags;

    @Schema(description = "累计销量")
    private Integer soldCount;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "商品状态，上架=1，下架=0", example = "1")
    private Integer status;
}



