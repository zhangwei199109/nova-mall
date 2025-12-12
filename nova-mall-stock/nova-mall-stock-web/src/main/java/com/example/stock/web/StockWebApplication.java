package com.example.stock.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.stock.service.mapper")
public class StockWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockWebApplication.class, args);
    }
}



