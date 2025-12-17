package com.example.product.web;

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
                        + "org.apache.dubbo.spring.boot.autoconfigure.DubboAutoConfiguration",
                "dubbo.registry.address=empty",
                "dubbo.config-center.address=",
                "dubbo.metadata-report.address=",
                "dubbo.application.metadata-type=local"
        })
class ProductWebIntegrationTest {

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

    @Test
    void list_should_return_products() throws Exception {
        ResponseEntity<String> resp = restTemplate.getForEntity(uri("/product"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(resp.getBody());
        assertThat(root.path("code").asInt()).isEqualTo(200);
        assertThat(root.path("data").isArray()).isTrue();
        assertThat(root.path("data").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void create_update_delete_product() throws Exception {
        // create
        String createPayload = """
                {"name":"Test Phone","description":"integration","price":999.00,"stock":10}
                """;
        ResponseEntity<String> createResp = restTemplate.postForEntity(uri("/product"), jsonEntity(createPayload), String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode created = objectMapper.readTree(createResp.getBody()).path("data");
        long id = created.path("id").asLong();
        assertThat(id).isPositive();

        // update
        String updatePayload = """
                {"name":"Test Phone Pro","description":"integration2","price":1099.00,"stock":8}
                """;
        ResponseEntity<String> updateResp = restTemplate.exchange(uri("/product/" + id), HttpMethod.PUT, jsonEntity(updatePayload), String.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // delete
        ResponseEntity<String> delResp = restTemplate.exchange(uri("/product/" + id), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode delRoot = objectMapper.readTree(delResp.getBody());
        assertThat(delRoot.path("code").asInt()).isEqualTo(200);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpEntity<String> jsonEntity(String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
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

