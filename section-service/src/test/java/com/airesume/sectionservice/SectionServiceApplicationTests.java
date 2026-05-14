package com.airesume.sectionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
/**
 * Bootstrap tests for the Section Management Microservice.
 * Ensures that the section-specific business context initializes correctly and
 * validates the main startup sequence.
 */
class SectionServiceApplicationTests {

	/**
	 * Verifies that the section-service application context starts up without errors.
	 */
	@Test
	void contextLoads() {
	}

	/**
	 * Tests the main application entry point with the test profile.
	 */
	@Test
	void testMain() {
		System.setProperty("spring.profiles.active", "test");
		// Run application main method with web environment disabled and test profile active
		SectionServiceApplication.main(new String[]{
				"--spring.main.web-application-type=none",
				"--spring.profiles.active=test"
		});
	}
}
