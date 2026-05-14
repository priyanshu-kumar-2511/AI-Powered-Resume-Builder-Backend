package com.airesume.exportservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Export Service.
 * This service handles the conversion of resumes into various formats (PDF, DOCX),
 * manages background export jobs via RabbitMQ, and performs periodic file cleanup.
 */
@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class ExportServiceApplication {

    /**
     * Entry point for the Export Service.
     * Loads environmental variables and starts the document generation engine.
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

		SpringApplication.run(ExportServiceApplication.class, args);
	}

}
