package com.example.ai.service.impl;

import com.example.ai.api.dto.PriceIntelRequest;
import com.example.ai.api.dto.PriceIntelResponse;
import com.example.ai.llm.LlmClient;
import com.example.ai.service.AiPriceService;
import com.example.ai.service.PriceIntelAgent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiPriceServiceImpl implements AiPriceService {

    private final PriceIntelAgent priceIntelAgent;
    private final LlmClient llmClient;

    public AiPriceServiceImpl(PriceIntelAgent priceIntelAgent, LlmClient llmClient) {
        this.priceIntelAgent = priceIntelAgent;
        this.llmClient = llmClient;
    }

    @Override
    public PriceIntelResponse advise(PriceIntelRequest req) {
        var ctx = new PriceIntelAgent.PriceContext(
                req.getCurrentPrice(),
                req.getBaselineConversion(),
                req.getConversionRate(),
                req.getCompetitorPrices(),
                req.getFloorPrice()
        );
        var advice = priceIntelAgent.advise(ctx);

        List<String> reasons = new ArrayList<>(advice.reasons());
        // 调用大模型给出补充解释（不调整价格，只做文字建议）
        String llm = llmClient.generatePlain(
                "你是电商价格分析助手，请结合给定数据，用不超过80字的中文给出调价建议与风险提示。",
                buildUserPrompt(req, advice)
        );
        if (llm != null && !llm.isBlank()) {
            reasons.add("LLM建议：" + llm.trim());
        }

        return new PriceIntelResponse(
                advice.action().name(),
                advice.suggestedPrice(),
                advice.pctChange(),
                reasons
        );
    }

    private String buildUserPrompt(PriceIntelRequest req, PriceIntelAgent.PriceAdvice advice) {
        return """
                当前价: %s
                竞品价: %s
                基线转化: %s
                当前转化: %s
                底价: %s
                规则建议: 动作=%s, 建议价=%s, 变动比例=%s, 原因=%s
                请给简短建议。
                """.formatted(
                req.getCurrentPrice(),
                req.getCompetitorPrices(),
                req.getBaselineConversion(),
                req.getConversionRate(),
                req.getFloorPrice(),
                advice.action().name(),
                advice.suggestedPrice(),
                advice.pctChange(),
                advice.reasons()
        );
    }
}

