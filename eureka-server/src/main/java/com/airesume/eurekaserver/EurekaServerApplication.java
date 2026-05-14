package com.airesume.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Main application class for the Eureka Service Discovery Server.
 * This server allows all microservices to register themselves and discover others
 * dynamically without hardcoded URLs.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    /**
     * Entry point for the Eureka Discovery Server.
     * Bootstrap the service registry with environmental configurations.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory("../")
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
