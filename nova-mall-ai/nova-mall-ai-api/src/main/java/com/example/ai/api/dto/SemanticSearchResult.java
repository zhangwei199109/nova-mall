package com.example.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语义搜索/推荐结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanticSearchResult {
    private String id;
    private String name;
    private String description;
    private Double score;
    private String reason;
}

