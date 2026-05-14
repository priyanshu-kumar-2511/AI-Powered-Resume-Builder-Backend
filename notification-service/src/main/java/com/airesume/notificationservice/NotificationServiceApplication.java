package com.airesume.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Notification Service.
 * This service acts as the communication hub for the platform, 
 * handling asynchronous email dispatch via RabbitMQ and in-app notifications.
 */
@SpringBootApplication
public class NotificationServiceApplication {

    /**
     * Entry point for the Notification Service.
     * Loads environment variables and starts the messaging listeners.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory("../")
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
