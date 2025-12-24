package com.example.order.web.service;

import com.example.order.web.service.llm.LlmClient;
import com.example.order.web.service.retrieve.FaqRetriever;
import com.example.order.web.service.retrieve.VectorRetriever;
import org.springframework.stereotype.Service;

@Service
public class AiQaService {

    private final FaqRetriever faqRetriever;
    private final LlmClient llmClient;
    private final VectorRetriever vectorRetriever;

    public AiQaService(FaqRetriever faqRetriever, LlmClient llmClient,
                       VectorRetriever vectorRetriever) {
        this.faqRetriever = faqRetriever;
        this.llmClient = llmClient;
        this.vectorRetriever = vectorRetriever;
    }

    public String answer(String question) {
        var contexts = vectorRetriever.retrieve(question, 3);
        if (contexts == null || contexts.isEmpty()) {
            contexts = faqRetriever.retrieve(question, 3);
        }
        return llmClient.generate(question, contexts);
    }
}

