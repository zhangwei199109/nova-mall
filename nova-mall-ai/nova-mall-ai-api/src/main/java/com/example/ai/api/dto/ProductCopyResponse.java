package com.example.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品文案生成结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCopyResponse {
    private String title;
    private List<String> bullets;
    private String description;
    private List<String> seoKeywords;
}

