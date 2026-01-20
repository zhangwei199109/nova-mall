package com.example.ai.service;

import com.example.ai.api.dto.PriceIntelRequest;
import com.example.ai.api.dto.PriceIntelResponse;

/**
 * 价格智能体应用服务。
 */
public interface AiPriceService {

    /**
     * 给出调价建议。
     */
    PriceIntelResponse advise(PriceIntelRequest req);
}



