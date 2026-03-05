package com.example.similarity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.similarity.controller", "com.example.similarity.service", "com.example.similarity.model"})
public class SimilarityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimilarityApplication.class, args);
    }
}
