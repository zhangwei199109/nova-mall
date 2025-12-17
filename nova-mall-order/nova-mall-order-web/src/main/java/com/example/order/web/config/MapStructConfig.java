package com.example.order.web.config;

import com.example.order.web.convert.OrderWebConvert;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapStructConfig {
    // OrderWebConvert 已使用 componentModel = "spring" 生成 Bean，显式声明防止扫描缺失。
    @Bean
    public OrderWebConvert orderWebConvert(OrderWebConvert mapper) {
        return mapper;
    }
}

