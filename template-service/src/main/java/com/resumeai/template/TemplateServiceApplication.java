package com.resumeai.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Entry Point for the Template Service.
 * This service manages resume templates, including their HTML/CSS layouts, 
 * categories, and tiers. It integrates with Eureka for discovery.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TemplateServiceApplication {

	public static void main(String[] args) {
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory("../")
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(TemplateServiceApplication.class, args);
	}

}
