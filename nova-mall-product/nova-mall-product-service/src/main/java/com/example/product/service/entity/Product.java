package com.example.product.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    /**
     * 类目ID，可用于多维筛选。
     */
    private Long categoryId;

    /**
     * 品牌名称。
     */
    private String brand;

    /**
     * 标签，逗号分隔。
     */
    private String tags;

    /**
     * 累计销量。
     */
    private Integer soldCount;

    /**
     * 累计浏览量。
     */
    private Integer viewCount;

    /**
     * 上架状态：1 上架，0 下架
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}



