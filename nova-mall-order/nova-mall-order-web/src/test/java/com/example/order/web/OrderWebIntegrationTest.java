package com.example.order.web;

import com.example.cart.api.CartApi;
import com.example.common.dto.Result;
import com.example.product.api.ProductApi;
import com.example.product.api.dto.ProductDTO;
import com.example.stock.api.StockApi;
import io.swagger.v3.oas.models.OpenAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderWebIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductApi productApi;

    @MockBean
    private StockApi stockApi;

    @MockBean
    private CartApi cartApi;

    @DynamicPropertySource
    static void sqlInit(DynamicPropertyRegistry registry) {
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:/db/init.sql");
    }

    @Test
    void list_should_return_orders() throws Exception {
        ResponseEntity<String> resp = restTemplate.getForEntity(uri("/order/list"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(resp.getBody());
        assertThat(root.path("code").asInt()).isEqualTo(200);
        assertThat(root.path("data").isArray()).isTrue();
        assertThat(root.path("data").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void create_and_pay_should_succeed_with_mocks() throws Exception {
        mockProductAndStock();
        mockCart();

        // create order with items provided，避免调用购物车
        String createPayload = """
                {"userId":1,"items":[{"productId":1,"quantity":2}]}
                """;
        ResponseEntity<String> createResp = restTemplate.postForEntity(uri("/order"), jsonEntity(createPayload), String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode created = objectMapper.readTree(createResp.getBody()).path("data");
        long orderId = created.path("id").asLong();
        assertThat(orderId).isPositive();

        // pay
        ResponseEntity<String> payResp = restTemplate.postForEntity(uri("/order/" + orderId + "/pay"), HttpEntity.EMPTY, String.class);
        assertThat(payResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(payResp.getBody()).path("code").asInt()).isEqualTo(200);
    }

    @Test
    void create_and_cancel_should_release_stock() throws Exception {
        mockProductAndStock();
        mockCart();

        String createPayload = """
                {"userId":2,"items":[{"productId":1,"quantity":1}]}
                """;
        ResponseEntity<String> createResp = restTemplate.postForEntity(uri("/order"), jsonEntity(createPayload), String.class);
        long orderId = objectMapper.readTree(createResp.getBody()).path("data").path("id").asLong();
        assertThat(orderId).isPositive();

        ResponseEntity<String> cancelResp = restTemplate.postForEntity(uri("/order/" + orderId + "/cancel"), HttpEntity.EMPTY, String.class);
        assertThat(cancelResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(cancelResp.getBody()).path("code").asInt()).isEqualTo(200);
    }

    private void mockProductAndStock() {
        ProductDTO p = new ProductDTO();
        p.setId(1L);
        p.setName("Mock Phone");
        p.setPrice(BigDecimal.valueOf(100));
        Mockito.when(productApi.get(eq(1L))).thenReturn(Result.success(p));

        Mockito.when(stockApi.reserve(any())).thenReturn(Result.success(true));
        Mockito.when(stockApi.release(any())).thenReturn(Result.success(true));
        Mockito.when(stockApi.deduct(any())).thenReturn(Result.success(true));
    }

    private void mockCart() {
        Mockito.when(cartApi.remove(any(), any())).thenReturn(Result.success(true));
        Mockito.when(cartApi.list(any())).thenReturn(Result.success(List.of()));
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
        OpenAPI openAPI() {
            return new OpenAPI();
        }
    }
}

