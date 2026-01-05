package com.example.pay.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PayWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayWebApplication.class, args);
    }
}







