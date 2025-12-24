package com.example.order.web.service.retrieve;

import com.example.order.web.config.AiQaProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 向量检索占位实现：调用可配置的 HTTP 向量服务（例如 ES kNN/Faiss 网关），
 * 若未配置或调用异常则返回空列表。
 */
@Component
public class VectorRetriever {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AiQaProperties properties;

    public VectorRetriever(AiQaProperties properties) {
        this.properties = properties;
    }

    public List<RetrievedDoc> retrieve(String question, int topK) {
        String endpoint = properties.getVectorEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return List.of();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "query", question,
                    "topK", topK <= 0 ? 3 : topK
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            VectorResponse resp = restTemplate.postForObject(endpoint, entity, VectorResponse.class);
            if (resp == null || resp.getHits() == null) {
                return List.of();
            }
            return resp.getHits().stream()
                    .map(hit -> new RetrievedDoc(hit.getTitle(), hit.getContent()))
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static class VectorResponse {
        private List<VectorHit> hits;

        public List<VectorHit> getHits() {
            return hits;
        }

        public void setHits(List<VectorHit> hits) {
            this.hits = hits;
        }
    }

    public static class VectorHit {
        private String title;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}


