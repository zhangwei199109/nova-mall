package com.example.cart.web.config;

import com.example.cart.web.convert.CartWebConvert;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapStructConfig {

    @Bean
    public CartWebConvert cartWebConvert() {
        return Mappers.getMapper(CartWebConvert.class);
    }
}

