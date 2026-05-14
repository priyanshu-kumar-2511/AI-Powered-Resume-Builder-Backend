package com.airesume.adminserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

/**
 * Main application class for the Spring Boot Admin Server.
 * This server provides a centralized dashboard for monitoring the health, 
 * logs, and metrics of all registered microservices.
 */
@SpringBootApplication
@EnableAdminServer
public class AdminServerApplication {

    /**
     * Entry point for the Admin Server.
     * Loads environment variables from .env file before starting the Spring context.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .directory("../")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(
            AdminServerApplication.class, args
        );
    }
}
