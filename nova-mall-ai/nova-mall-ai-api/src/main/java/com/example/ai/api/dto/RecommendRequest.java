package com.example.ai.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * 商品推荐请求：基于商品ID或标签偏好返回相似/相关推荐。
 */
@Data
public class RecommendRequest {

    /**
     * 参考商品ID（若提供则优先做相似推荐）。
     */
    private String productId;

    /**
     * 期望的标签/偏好，如「运动」「轻便」「降噪」。
     */
    private List<String> preferTags;

    @Min(value = 1, message = "topK 最少 1 条")
    @Max(value = 20, message = "topK 最多 20 条")
    private Integer topK = 5;
}



