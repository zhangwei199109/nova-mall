package com.example.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品查询参数")
public class ProductQuery {

    @Schema(description = "名称关键词（模糊）")
    private String keyword;

    @Schema(description = "状态：1上架，0下架，空则默认筛选上架")
    private Integer status;

    @Schema(description = "最低价格")
    private BigDecimal minPrice;

    @Schema(description = "最高价格")
    private BigDecimal maxPrice;

    @Schema(description = "类目ID")
    private Long categoryId;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "排序字段：price/sales/newest，默认更新时间")
    private String sortBy;

    @Schema(description = "排序方向：asc/desc，默认desc")
    private String order;
}








