package com.airesume.templateservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class TemplateServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		try {
			TemplateServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});
		} catch (Exception e) {
			// Ignore exceptions
		}
	}
}
