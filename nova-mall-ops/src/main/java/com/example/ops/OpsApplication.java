package com.example.ops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.ops.mapper")
@EnableFeignClients(basePackages = "com.example")
public class OpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpsApplication.class, args);
    }
}

















