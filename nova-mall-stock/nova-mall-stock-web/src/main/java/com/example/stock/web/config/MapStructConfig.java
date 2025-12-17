package com.example.stock.web.config;

import com.example.stock.web.convert.StockWebConvert;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapStructConfig {

    @Bean
    public StockWebConvert stockWebConvert() {
        return Mappers.getMapper(StockWebConvert.class);
    }
}

