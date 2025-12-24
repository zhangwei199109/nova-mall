package com.example.order.web.service.llm;

import com.example.order.web.config.AiQaProperties;
import com.example.order.web.service.retrieve.RetrievedDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM HTTP 客户端：优先调用可配置的大模型服务，失败时回退本地拼接回答。
 */
@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final AiQaProperties properties;

    public LlmClient(AiQaProperties properties) {
        this.properties = properties;
    }

    public String generate(String question, List<RetrievedDoc> contexts) {
        String answer = callAliLlm(question, contexts);
        if (answer != null && !answer.isBlank()) {
            return answer;
        }
        // 本地回退逻辑
        if (contexts == null || contexts.isEmpty()) {
            return "抱歉，暂未找到相关答案，请稍后重试或联系人工客服。";
        }
        String joined = contexts.stream()
                .map(doc -> "- " + doc.content())
                .collect(Collectors.joining("\n"));
        return """
                您问到：“%s”
                根据检索到的信息，供参考：
                %s
                （本回答由内置模型生成，可联系人工客服获取更多帮助）
                """.formatted(question, joined);
    }

    private String callAliLlm(String question, List<RetrievedDoc> contexts) {
        String endpoint = properties.getLlmEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        }
        String model = (properties.getLlmModel() == null || properties.getLlmModel().isBlank())
                ? "qwen-turbo"
                : properties.getLlmModel();
        if (properties.getLlmApiKey() == null || properties.getLlmApiKey().isBlank()) {
            return null;
        }
        try {
            log.info("LLM request: endpoint={}, model={}", endpoint, model);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getLlmApiKey());
            String contextText = buildContextText(contexts);
            String userPrompt = buildUserPrompt(question, contextText);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "input", Map.of(
                            "messages", List.of(
                                    Map.of("role", "system", "content", "你是一个电商客服助手，请结合提供的上下文信息简洁回答用户问题。"),
                                    Map.of("role", "user", "content", userPrompt)
                            )
                    ),
                    "parameters", Map.of("temperature", 0.3)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            LlmResponse resp = restTemplate.postForObject(endpoint, entity, LlmResponse.class);
            if (resp == null) {
                return null;
            }
            // DashScope text-generation: output.text
            if (resp.output != null && resp.output.text != null && !resp.output.text.isBlank()) {
                log.info("LLM response via output.text: {}", preview(resp.output.text));
                return resp.output.text;
            }
            // Chat style: choices[].message.content
            if (resp.output != null && resp.output.choices != null && !resp.output.choices.isEmpty()) {
                LlmChoice first = resp.output.choices.get(0);
                if (first != null && first.message != null && first.message.content != null) {
                    log.info("LLM response via output.choices: {}", preview(first.message.content));
                    return first.message.content;
                }
            }
            if (resp.choices != null && !resp.choices.isEmpty()) {
                LlmChoice first = resp.choices.get(0);
                if (first != null && first.message != null && first.message.content != null) {
                    log.info("LLM response via choices: {}", preview(first.message.content));
                    return first.message.content;
                }
            }
        } catch (Exception e) {
            log.warn("LLM request failed, fallback to local. endpoint={}, model={}, err={}",
                    endpoint, model, e.toString());
            return null;
        }
        return null;
    }

    /** 打印响应预览，避免日志过长 */
    private String preview(String text) {
        if (text == null) {
            return null;
        }
        int max = 500;
        return text.length() > max ? text.substring(0, max) + "...(truncated)" : text;
    }

    private String buildContextText(List<RetrievedDoc> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return "暂无可用上下文。";
        }
        return contexts.stream()
                .map(doc -> doc.title() + "：" + doc.content())
                .collect(Collectors.joining("\n"));
    }

    private String buildUserPrompt(String question, String contextText) {
        return """
                问题：%s
                相关上下文：
                %s
                请基于上述上下文简洁回答问题。若未提及，请说明“未在已知信息中找到”。""".formatted(question, contextText);
    }

    public static class LlmResponse {
        public Output output;
        public List<LlmChoice> choices;
    }

    public static class Output {
        public String text;
        public List<LlmChoice> choices;
    }

    public static class LlmChoice {
        public Message message;
    }

    public static class Message {
        public String role;
        public String content;
    }
}

