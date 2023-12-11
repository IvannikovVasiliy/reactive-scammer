package ru.neoflex.scammertracking.analyzer.config.kafka;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DestroyConfig {

    @Autowired
    public DestroyConfig(@Qualifier("createConsumer") Consumer<String, byte[]> consumer,
                         @Qualifier("createBackoffConsumer")Consumer<String, byte[]> backoffConsumer) {
        this.consumer = consumer;
        this.backoffConsumer = backoffConsumer;
    }

    private final Consumer<String, byte[]> consumer;
    private final Consumer<String, byte[]> backoffConsumer;

    @PreDestroy
    public void closeConsumer() {
        consumer.commitSync();
        consumer.close();

        backoffConsumer.commitSync();
        backoffConsumer.close();
    }
}
