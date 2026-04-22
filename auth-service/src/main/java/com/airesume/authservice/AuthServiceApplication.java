package com.airesume.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Application class for the Auth Service.
 * 
 * This service handles identity management, user registration, 
 * authentication using JWT, and account recovery flows (Username/Password).
 * 
 * @author ResumeAI Team
 * @version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        // Load .env file from the project root
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .directory("../") // Look at the root folder
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
