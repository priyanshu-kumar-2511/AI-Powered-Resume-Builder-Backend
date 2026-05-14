package com.airesume.resumeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Entry Point for the Resume Service.
 * Manages the core resume lifecycle, including creation from templates,
 * public gallery publishing, and caching of resume metadata.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableFeignClients
public class ResumeServiceApplication {

    /**
     * Entry point for the Resume Service.
     * Bootstrap the application with environment variables and starts the context.
     * 
     * @param args command line arguments
     */
	public static void main(String[] args) {
		// Load .env file from the project root
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory("../") // Look at the root folder
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(ResumeServiceApplication.class, args);
	}
}
