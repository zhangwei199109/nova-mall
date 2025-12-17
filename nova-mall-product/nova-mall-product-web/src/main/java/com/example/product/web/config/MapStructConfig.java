package com.example.product.web.config;

import com.example.product.web.convert.ProductWebConvert;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapStructConfig {

    @Bean
    public ProductWebConvert productWebConvert() {
        return Mappers.getMapper(ProductWebConvert.class);
    }
}

