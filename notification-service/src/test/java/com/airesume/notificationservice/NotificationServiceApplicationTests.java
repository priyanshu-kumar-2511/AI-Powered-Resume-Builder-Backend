package com.airesume.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		System.setProperty("spring.profiles.active", "test");
		NotificationServiceApplication.main(new String[] {
				"--spring.profiles.active=test",
				"--spring.main.web-application-type=none"
		});
	}
}
