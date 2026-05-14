package com.airesume.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Main application class for the Centralized Configuration Server.
 * This server provides shared configuration (YAML/properties) to all microservices
 * in the ResumeAI ecosystem.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    /**
     * Entry point for the Config Server.
     * Loads system-level environment variables before serving configurations.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory("../")
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
