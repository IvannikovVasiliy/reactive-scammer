package ru.neoflex.scammertracking.analyzer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
public class AnalyzerApp {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApp.class, args);
    }
}
