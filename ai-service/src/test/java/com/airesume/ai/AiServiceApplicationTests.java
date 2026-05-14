package com.airesume.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Baseline integration tests for the AI Microservice.
 * Ensures that the Spring application context initializes correctly and
 * validates the main entry point logic.
 */
@SpringBootTest
@ActiveProfiles("test")
class AiServiceApplicationTests {

    /**
     * Verifies that the application context loads successfully.
     */
    @Test
    void contextLoads() {
        // Basic test to ensure context starts up and covers the Application class
    }
    
    /**
     * Validates the main method execution path.
     */
    @Test
    void mainMethodTest() {
        // Covering the main method
        AiServiceApplication.main(new String[]{"--spring.profiles.active=test"});
    }
}
