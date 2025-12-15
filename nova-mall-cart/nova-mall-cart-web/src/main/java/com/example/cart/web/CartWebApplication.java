package com.example.cart.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.cart.service.mapper")
public class CartWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartWebApplication.class, args);
    }
}

