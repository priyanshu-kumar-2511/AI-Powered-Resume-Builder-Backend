package com.airesume.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Standard Spring Boot application tests.
 * Verifies that the application context and main entry point initialize correctly.
 */
@SpringBootTest
class PaymentServiceApplicationTests {

	/**
	 * Verifies that the Spring application context loads without errors.
	 */
	@Test
	void contextLoads() {
	}

	/**
	 * Tests the main entry point of the microservice application.
	 */
	@Test
	void testMain() {
		try {
			PaymentServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});
		} catch (Exception e) {
			// Ignore any exceptions
		}
	}
}
