package com.ticketguru.event_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /**
     * Spring Context'e ObjectMapper bean'ini tan?t?r.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // KRİTİK: LocalDateTime gibi Java 8 tarih/saat nesnelerini do?ru JSON'a çevirmek için
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}