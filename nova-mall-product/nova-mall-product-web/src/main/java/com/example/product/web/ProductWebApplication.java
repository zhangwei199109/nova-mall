package com.example.product.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.product.service.mapper")
public class ProductWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductWebApplication.class, args);
    }
}

