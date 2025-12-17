package com.example.cart.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.openfeign.enabled=false",
                "spring.autoconfigure.exclude="
                        + "org.springframework.cloud.openfeign.FeignAutoConfiguration,"
                        + "org.apache.dubbo.spring.boot.autoconfigure.DubboAutoConfiguration,"
                        + "org.apache.dubbo.spring.boot.autoconfigure.DubboConfiguration,"
                        + "org.apache.dubbo.spring.boot.autoconfigure.DubboRelaxedConfiguration,"
                        + "org.apache.dubbo.spring.boot.autoconfigure.DubboMetadataAutoConfiguration",
                "dubbo.registry.address=empty",
                "dubbo.config-center.address=",
                "dubbo.metadata-report.address=",
                "dubbo.application.metadata-type=local"
        })
class CartWebIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void sqlInit(DynamicPropertyRegistry registry) {
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:/db/init.sql");
    }

    private static final String USER = "guest";

    @Test
    void list_should_return_items() throws Exception {
        ResponseEntity<String> resp = restTemplate.exchange(uri("/cart"), HttpMethod.GET, withUser(null), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(resp.getBody());
        assertThat(root.path("code").asInt()).isEqualTo(200);
        assertThat(root.path("data").isArray()).isTrue();
    }

    @Test
    void add_update_remove_clear_flow() throws Exception {
        // add
        String payload = """
                {"productId":99,"productName":"Tmp","price":9.9,"quantity":1}
                """;
        ResponseEntity<String> addResp = restTemplate.postForEntity(uri("/cart"), withUser(payload), String.class);
        assertThat(addResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // update
        ResponseEntity<String> updResp = restTemplate.exchange(uri("/cart/99?quantity=2"), HttpMethod.PUT, withUser(null), String.class);
        assertThat(updResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // remove
        ResponseEntity<String> delResp = restTemplate.exchange(uri("/cart/99"), HttpMethod.DELETE, withUser(null), String.class);
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // clear
        ResponseEntity<String> clearResp = restTemplate.exchange(uri("/cart"), HttpMethod.DELETE, withUser(null), String.class);
        assertThat(clearResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpEntity<String> withUser(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", USER);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }

    @TestConfiguration
    static class OpenApiTestConfig {
        @Bean
        @Primary
        io.swagger.v3.oas.models.OpenAPI openAPI() {
            return new io.swagger.v3.oas.models.OpenAPI();
        }
    }
}

