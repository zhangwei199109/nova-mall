package com.example.ai.service.impl;

import com.example.ai.llm.LlmClient;
import com.example.ai.retrieve.FaqRetriever;
import com.example.ai.retrieve.VectorRetriever;
import com.example.ai.retrieve.RetrievedDoc;
import com.example.ai.service.AiQaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI 问答领域服务实现。
 */
@Service
public class AiQaServiceImpl implements AiQaService {

    private final FaqRetriever faqRetriever;
    private final LlmClient llmClient;
    private final VectorRetriever vectorRetriever;

    public AiQaServiceImpl(FaqRetriever faqRetriever, LlmClient llmClient,
                           VectorRetriever vectorRetriever) {
        this.faqRetriever = faqRetriever;
        this.llmClient = llmClient;
        this.vectorRetriever = vectorRetriever;
    }

    @Override
    public String answer(String question) {
        var contexts = retrieveContexts(question);
        return llmClient.generate(question, contexts);
    }

    @Override
    public String answerWithContexts(String question, List<RetrievedDoc> contexts) {
        return llmClient.generate(question, contexts);
    }

    @Override
    public List<RetrievedDoc> retrieveContexts(String question) {
        var contexts = vectorRetriever.retrieve(question, 3);
        if (contexts == null || contexts.isEmpty()) {
            contexts = faqRetriever.retrieve(question, 3);
        }
        return contexts;
    }

    @Override
    public boolean streamAnswer(String question, List<RetrievedDoc> contexts, Consumer<String> onChunk) {
        return llmClient.stream(question, contexts, onChunk);
    }
}

