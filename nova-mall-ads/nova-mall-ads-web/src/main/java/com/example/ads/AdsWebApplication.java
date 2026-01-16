package com.example.ads;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.ads.service.channel.ChannelConfigProperties;

/**
 * 广告服务启动类，扫描 mapper 并启动 Spring Boot。
 */
@SpringBootApplication
@MapperScan("com.example.ads.service.mapper")
@EnableConfigurationProperties(ChannelConfigProperties.class)
public class AdsWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdsWebApplication.class, args);
    }
}

