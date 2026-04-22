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
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory("../")
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
