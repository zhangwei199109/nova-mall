package com.example.ai.service.impl;

import com.example.ai.api.dto.ProductCopyRequest;
import com.example.ai.api.dto.ProductCopyResponse;
import com.example.ai.api.dto.SemanticSearchRequest;
import com.example.ai.api.dto.SemanticSearchResult;
import com.example.ai.llm.LlmClient;
import com.example.ai.retrieve.ProductRetriever;
import com.example.ai.service.AiProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AiProductServiceImpl implements AiProductService {

    private final LlmClient llmClient;
    private final ProductRetriever productRetriever;

    public AiProductServiceImpl(LlmClient llmClient, ProductRetriever productRetriever) {
        this.llmClient = llmClient;
        this.productRetriever = productRetriever;
    }

    @Override
    public ProductCopyResponse generateCopy(ProductCopyRequest req) {
        // 商品文案生成：
        // 1) 组装 prompt 调用 LLM，生成结构化 JSON（LlmClient 内处理）
        // 2) 若 LLM 失败或解析异常，则使用本地 fallback 文案兜底
        return llmClient.generateProductCopy(req);
    }

    @Override
    public List<SemanticSearchResult> semanticSearch(SemanticSearchRequest req) {
        // 简易语义/关键词检索：
        // 1) 使用内置商品样例做关键词/标签打分
        // 2) 后续可替换为真实向量召回，这里保留 topK 入参
        int topK = req.getTopK() == null ? 5 : req.getTopK();
        String query = StringUtils.trimWhitespace(req.getQuery());
        return productRetriever.search(query, topK);
    }
}

