package com.airesume.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bootstrap tests for the Authentication Microservice.
 * Ensures that the identity management context initializes correctly and
 * validates the main startup sequence.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

    /**
     * Verifies that the auth-service application context starts up without errors.
     */
    @Test
    void contextLoads() {
        assertTrue(true, "Spring context loaded successfully");
    }

    /**
     * Tests the main application entry point.
     */
    @Test
    void testMain() {
        assertDoesNotThrow(() -> {
            AuthServiceApplication.main(new String[]{"--server.port=0", "--spring.profiles.active=test"});
        });
    }
}
