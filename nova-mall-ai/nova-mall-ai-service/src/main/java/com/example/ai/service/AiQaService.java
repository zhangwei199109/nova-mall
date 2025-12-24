package com.example.ai.service;

import com.example.ai.retrieve.RetrievedDoc;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI 问答领域服务接口，提供同步与流式能力。
 */
public interface AiQaService {
    /**
     * 同步问答：检索上下文后调用 LLM 返回完整回答。
     */
    String answer(String question);

    /**
     * 使用已给定的上下文直接生成回答。
     */
    String answerWithContexts(String question, List<RetrievedDoc> contexts);

    /**
     * 检索上下文（向量检索优先，空则回退 FAQ）。
     */
    List<RetrievedDoc> retrieveContexts(String question);

    /**
     * 流式问答：调用 LLM 流接口并逐段回调；返回 true 表示已尝试流式。
     */
    boolean streamAnswer(String question, List<RetrievedDoc> contexts, Consumer<String> onChunk);
}

