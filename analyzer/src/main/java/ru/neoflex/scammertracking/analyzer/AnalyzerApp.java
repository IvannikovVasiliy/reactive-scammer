package ru.neoflex.scammertracking.analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class AnalyzerApp {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApp.class, args);
    }
}
