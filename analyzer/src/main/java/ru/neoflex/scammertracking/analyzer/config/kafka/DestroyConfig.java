package ru.neoflex.scammertracking.analyzer.config.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DestroyConfig {

    private final Consumer<String, byte[]> consumer;

    @PreDestroy
    public void destroy() {
        consumer.commitSync();
        consumer.close();
    }
}
