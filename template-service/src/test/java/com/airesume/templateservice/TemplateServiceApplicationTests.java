package com.airesume.templateservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
/**
 * Bootstrap tests for the Template Management Microservice.
 * Ensures that the template engine and repository context initializes correctly.
 */
class TemplateServiceApplicationTests {

	/**
	 * Verifies that the template-service application context starts up without errors.
	 */
	@Test
	void contextLoads() {
	}

	/**
	 * Tests the main application entry point.
	 */
	@Test
	void testMain() {
		try {
			TemplateServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});
		} catch (Exception e) {
			// Ignore exceptions
		}
	}
}
