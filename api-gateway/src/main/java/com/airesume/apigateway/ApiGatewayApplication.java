package com.airesume.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the ResumeAI API Gateway.
 * This gateway acts as the single entry point for all client requests,
 * providing routing, security, and load balancing across the microservices.
 */
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
