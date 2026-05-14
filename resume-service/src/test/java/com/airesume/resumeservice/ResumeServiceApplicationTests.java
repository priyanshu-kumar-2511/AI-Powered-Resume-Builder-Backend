package com.airesume.resumeservice;

import com.airesume.resumeservice.client.SectionServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bootstrap tests for the Resume Microservice.
 * Ensures that the core resume management context initializes correctly and
 * validates the main startup sequence.
 */
@SpringBootTest
@ActiveProfiles("test")
class ResumeServiceApplicationTests {

    @MockitoBean
    private SectionServiceClient sectionServiceClient;

    /**
     * Verifies that the resume-service application context starts up without errors.
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
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            ResumeServiceApplication.main(new String[]{"--server.port=0"});
        });
    }
}
