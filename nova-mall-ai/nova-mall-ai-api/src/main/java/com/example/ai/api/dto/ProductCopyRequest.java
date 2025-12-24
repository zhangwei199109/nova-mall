package com.example.ai.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 商品文案生成请求。
 */
@Data
public class ProductCopyRequest {

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    /**
     * 商品卖点/特性，如「纯棉」「速干」「轻薄」。
     */
    private List<String> highlights;

    /**
     * 推广渠道，如「天猫详情」「短视频」「社媒」。
     */
    private String channel;

    /**
     * 语气/风格，如「专业」「活泼」「简洁」。
     */
    private String tone;

    /**
     * 目标人群，如「大学生」「户外人群」。
     */
    private String audience;

    /**
     * 语言，默认 zh-CN。
     */
    private String language = "zh-CN";

    @Min(value = 2, message = "卖点最少 2 条")
    @Max(value = 8, message = "卖点最多 8 条")
    private Integer bulletCount = 4;

    /**
     * 描述长度（字数上限，提示用）。
     */
    @Min(value = 30, message = "描述长度至少 30")
    @Max(value = 400, message = "描述长度最多 400")
    private Integer descriptionLength = 160;
}

