package com.example.ai.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 语义搜索/推荐请求。
 */
@Data
public class SemanticSearchRequest {

    @NotBlank(message = "查询词不能为空")
    private String query;

    @Min(value = 1, message = "topK 最少 1 条")
    @Max(value = 20, message = "topK 最多 20 条")
    private Integer topK = 5;
}

