package com.airesume.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Bootstrap tests for the Notification Microservice.
 * Ensures that the message dispatch and delivery context initializes correctly.
 */
@SpringBootTest
class NotificationServiceApplicationTests {

	/**
	 * Verifies that the notification-service application context starts up without errors.
	 */
	@Test
	void contextLoads() {
	}

	/**
	 * Tests the main application entry point.
	 */
	@Test
	void testMain() {
		System.setProperty("spring.profiles.active", "test");
		NotificationServiceApplication.main(new String[] {
				"--spring.profiles.active=test",
				"--spring.main.web-application-type=none"
		});
	}
}
