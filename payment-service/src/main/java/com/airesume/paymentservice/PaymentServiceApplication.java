package com.airesume.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application class for the Payment Service.
 * This service manages the subscription lifecycle, Razorpay order creation,
 * payment verification, and synchronization with the Authentication service.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PaymentServiceApplication {

    /**
     * Entry point for the Payment Service.
     * Bootstrap the service with environment variables loaded from .env.
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

        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
