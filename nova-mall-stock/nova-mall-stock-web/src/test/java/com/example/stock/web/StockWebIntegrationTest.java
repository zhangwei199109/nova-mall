package com.example.stock.web;

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
class StockWebIntegrationTest {

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
    void get_reserve_release_deduct() throws Exception {
        // get
        ResponseEntity<String> getResp = restTemplate.getForEntity(uri("/stock/1"), String.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode getRoot = objectMapper.readTree(getResp.getBody());
        assertThat(getRoot.path("code").asInt()).isEqualTo(200);

        // reserve
        String reserve = """
                {"productId":1,"quantity":2}
                """;
        ResponseEntity<String> resResp = restTemplate.postForEntity(uri("/stock/reserve"), json(reserve), String.class);
        assertThat(resResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // release
        ResponseEntity<String> relResp = restTemplate.postForEntity(uri("/stock/release"), json(reserve), String.class);
        assertThat(relResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // deduct (again reserve then deduct to keep状态合理)
        ResponseEntity<String> res2 = restTemplate.postForEntity(uri("/stock/reserve"), json(reserve), String.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<String> dedResp = restTemplate.postForEntity(uri("/stock/deduct"), json(reserve), String.class);
        assertThat(dedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpEntity<String> json(String payload) {
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

