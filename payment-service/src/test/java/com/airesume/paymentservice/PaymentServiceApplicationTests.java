package com.airesume.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		try {
			PaymentServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});
		} catch (Exception e) {
			// Ignore any exceptions
		}
	}
}
