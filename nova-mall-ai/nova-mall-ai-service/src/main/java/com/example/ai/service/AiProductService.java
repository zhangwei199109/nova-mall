package com.example.ai.service;

import com.example.ai.api.dto.ProductCopyRequest;
import com.example.ai.api.dto.ProductCopyResponse;
import com.example.ai.api.dto.SemanticSearchRequest;
import com.example.ai.api.dto.SemanticSearchResult;
import com.example.ai.api.dto.RecommendRequest;

import java.util.List;

/**
 * AI 商品相关能力：文案生成、语义搜索/推荐。
 */
public interface AiProductService {

    /**
     * 生成商品文案（标题/卖点/描述等）。
     */
    ProductCopyResponse generateCopy(ProductCopyRequest req);

    /**
     * 语义搜索/推荐商品。
     */
    List<SemanticSearchResult> semanticSearch(SemanticSearchRequest req);

    /**
     * 相似/猜你喜欢推荐。
     */
    List<SemanticSearchResult> recommend(RecommendRequest req);
}

