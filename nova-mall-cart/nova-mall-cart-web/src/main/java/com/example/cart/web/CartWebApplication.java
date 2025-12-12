package com.example.cart.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.cart.service.mapper")
@EnableDubbo(scanBasePackages = "com.example")
public class CartWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartWebApplication.class, args);
    }
}

