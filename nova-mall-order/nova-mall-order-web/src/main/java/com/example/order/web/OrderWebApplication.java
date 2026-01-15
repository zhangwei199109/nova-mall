package com.example.order.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.order.service.mapper")
@EnableFeignClients(basePackages = "com.example")
@EnableScheduling
public class OrderWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderWebApplication.class, args);
    }
}



