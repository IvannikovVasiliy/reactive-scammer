package ru.neoflex.scammertracking.analyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@Slf4j
public class AnalyzerConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AtomicBoolean isRedisDropped() {
        return new AtomicBoolean(false);
    }

    @Bean
    public Map<Long, WrapPaymentRequestDto> storage() {
        return new ConcurrentHashMap<>();
    }
}
