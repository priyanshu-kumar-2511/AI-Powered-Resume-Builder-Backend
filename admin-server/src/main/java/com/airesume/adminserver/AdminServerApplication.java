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

    public static void main(String[] args) {
        SpringApplication.run(
            AdminServerApplication.class, args
        );
    }
}