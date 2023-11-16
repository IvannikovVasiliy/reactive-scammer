package ru.neoflex.scammertracking.analyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@Slf4j
public class AnalyzerConfig {

    @Value("${spring.kafka.bootstrapAddress}")
    private String url;

    @Bean
    public ObjectMapper objectMapper() {
        log.info(url);
        return new ObjectMapper();
    }
}
