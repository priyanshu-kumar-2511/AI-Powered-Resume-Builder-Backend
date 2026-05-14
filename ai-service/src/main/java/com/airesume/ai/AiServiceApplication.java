package com.airesume.ai;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for the AI Service.
 * This service provides AI-driven resume optimization, ATS score calculation,
 * and content generation features using Large Language Models.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class AiServiceApplication {

    /**
     * Entry point for the AI Service.
     * Loads environmental configurations from .env file before starting the context.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Load .env variables from the root project directory
        Dotenv dotenv = Dotenv.configure()
                .directory("../")
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
