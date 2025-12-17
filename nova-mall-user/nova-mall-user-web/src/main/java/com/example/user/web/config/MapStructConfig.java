package com.example.user.web.config;

import com.example.user.web.convert.UserWebConvert;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapStructConfig {

    @Bean
    public UserWebConvert userWebConvert() {
        return Mappers.getMapper(UserWebConvert.class);
    }
}

