package com.example.user.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户服务 Web 层集成测试（基于内存 H2 + 随机端口）。
 * 覆盖登录与受保护接口（模拟网关透传身份头）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserWebIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_should_issue_tokens() throws Exception {
        URI uri = URI.create("http://localhost:" + port + "/auth/login");
        String payload = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        ResponseEntity<String> resp = restTemplate.postForEntity(uri, buildJsonEntity(payload), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(resp.getBody());
        assertThat(root.path("code").asInt()).isEqualTo(200);
        assertThat(root.path("data").path("token").asText()).isNotBlank();
        assertThat(root.path("data").path("refreshToken").asText()).isNotBlank();
    }

    @Test
    void list_should_require_auth_headers() {
        URI uri = URI.create("http://localhost:" + port + "/user/list");
        ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void list_should_return_users_when_headers_present() throws Exception {
        URI uri = URI.create("http://localhost:" + port + "/user/list");
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "U-admin");
        headers.set("X-User-Name", "admin");
        headers.set("X-User-Roles", "ROLE_ADMIN,ROLE_USER");

        ResponseEntity<String> resp = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(resp.getBody());
        assertThat(root.path("code").asInt()).isEqualTo(200);
        assertThat(root.path("data").isArray()).isTrue();
        assertThat(root.path("data").size()).isGreaterThanOrEqualTo(1);
    }

    private HttpEntity<String> buildJsonEntity(String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }
}

