package com.example.ai.llm;

import com.example.ai.config.AiQaProperties;
import com.example.ai.retrieve.RetrievedDoc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final RestTemplate restTemplate;
    private final AiQaProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmClient(AiQaProperties properties) {
        this.properties = properties;
        this.restTemplate = buildRestTemplate(properties);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
    }

    public String generate(String question, List<RetrievedDoc> contexts) {
        String answer = callAliLlm(question, contexts);
        if (answer != null && !answer.isBlank()) {
            return answer;
        }
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
        int retry = Math.max(0, properties.getRetry());
        for (int attempt = 0; attempt <= retry; attempt++) {
            try {
                log.info("LLM request: endpoint={}, model={}, attempt={}", endpoint, model, attempt + 1);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(properties.getLlmApiKey());
                String contextText = buildContextText(contexts);
                Map<String, Object> body = getStringObjectMap(question, contextText, model);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                LlmResponse resp = restTemplate.postForObject(endpoint, entity, LlmResponse.class);
                if (resp == null) {
                    continue;
                }
                if (resp.output != null && resp.output.text != null && !resp.output.text.isBlank()) {
                    log.info("LLM response via output.text: {}", preview(resp.output.text));
                    return resp.output.text;
                }
                if (resp.output != null && resp.output.choices != null && !resp.output.choices.isEmpty()) {
                    LlmChoice first = resp.output.choices.getFirst();
                    if (first != null && first.message != null && first.message.content != null) {
                        log.info("LLM response via output.choices: {}", preview(first.message.content));
                        return first.message.content;
                    }
                }
                if (resp.choices != null && !resp.choices.isEmpty()) {
                    LlmChoice first = resp.choices.getFirst();
                    if (first != null && first.message != null && first.message.content != null) {
                        log.info("LLM response via choices: {}", preview(first.message.content));
                        return first.message.content;
                    }
                }
            } catch (Exception e) {
                if (attempt >= retry) {
                    log.warn("LLM request failed, fallback to local. endpoint={}, model={}, err={}",
                            endpoint, model, e.toString());
                    return null;
                }
                log.warn("LLM request failed, will retry. attempt={}, err={}", attempt + 1, e.toString());
            }
        }
        return null;
    }

    private @NonNull Map<String, Object> getStringObjectMap(String question, String contextText, String model) {
        String userPrompt = buildUserPrompt(question, contextText);

        return Map.of(
                "model", model,
                "input", Map.of(
                        "messages", List.of(
        Map.of("role", "system", "content", systemPrompt()),
        Map.of("role", "user", "content", userPrompt)
                        )
                ),
                "parameters", Map.of("temperature", 0.3)
        );
    }

    public boolean stream(String question, List<RetrievedDoc> contexts, Consumer<String> onChunk) {
        String endpoint = properties.getLlmEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        }
        String model = (properties.getLlmModel() == null || properties.getLlmModel().isBlank())
                ? "qwen-turbo"
                : properties.getLlmModel();
        if (properties.getLlmApiKey() == null || properties.getLlmApiKey().isBlank()) {
            log.warn("LLM stream skipped: apiKey missing");
            return false;
        }
        boolean anySent = false;
        try {
            String contextText = buildContextText(contexts);
            Map<String, Object> body = getObjectMap(question, contextText, model);
            String payload = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + properties.getLlmApiKey())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(properties.getStreamTimeoutMs()))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            log.info("LLM stream request: endpoint={}, model={}", endpoint, model);
            HttpResponse<InputStream> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() / 100 != 2) {
                log.warn("LLM stream non-2xx, status={}, will fallback", resp.statusCode());
                return false;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    if (line.startsWith("data:")) {
                        line = line.substring(5).trim();
                    }
                    if ("[DONE]".equals(line)) {
                        break;
                    }
                    String delta = extractDelta(line);
                    if (delta != null && !delta.isBlank()) {
                        onChunk.accept(delta);
                        anySent = true;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("LLM stream failed, will fallback. endpoint={}, model={}, err={}", endpoint, model, e.toString());
            return false;
        }
        return anySent;
    }

    private @NonNull Map<String, Object> getObjectMap(String question, String contextText, String model) {
        String userPrompt = buildUserPrompt(question, contextText);
        return Map.of(
                "model", model,
                "input", Map.of(
                        "messages", List.of(
            Map.of("role", "system", "content", systemPrompt()),
            Map.of("role", "user", "content", userPrompt)
                        )
                ),
                "parameters", Map.of("temperature", 0.3),
                "stream", true
        );
    }

    private String extractDelta(String line) {
        try {
            JsonNode node = objectMapper.readTree(line);
            if (node == null) {
                return null;
            }
            JsonNode output = node.get("output");
            if (output != null) {
                JsonNode text = output.get("text");
                if (text != null && !text.asText().isBlank()) {
                    return text.asText();
                }
                JsonNode choices = output.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    JsonNode first = choices.get(0);
                    if (first != null) {
                        JsonNode msg = first.get("message");
                        if (msg != null) {
                            JsonNode content = msg.get("content");
                            if (content != null && !content.asText().isBlank()) {
                                return content.asText();
                            }
                        }
                    }
                }
            }
            JsonNode choices = node.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode first = choices.get(0);
                if (first != null) {
                    JsonNode msg = first.get("message");
                    if (msg != null) {
                        JsonNode content = msg.get("content");
                        if (content != null && !content.asText().isBlank()) {
                            return content.asText();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return line;
    }

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
                请基于上述上下文简洁回答问题。若未提及，请明确回复“未在已知信息中找到”，并给出简短建议（如联系客服、提供更多信息）。""".formatted(question, contextText);
    }

    private String systemPrompt() {
        return """
            你是一个电商智能客服，仅依据提供的上下文回答：
            - 若上下文包含答案，务必引用其中的关键信息，用简洁中文作答，可分点列出。
            - 若上下文未提及，请明确回复“未在已知信息中找到”，可附简短建议（如联系客服、补充信息）。
            - 不要编造、不要夸大、不要输出与电商场景无关的内容。
            - 控制长度，优先直接结论，必要时给出 1-3 条关键要点。""";
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
        public String content;
    }

    private RestTemplate buildRestTemplate(AiQaProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }
}

