package com.airesume.sectionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class SectionServiceApplicationTests {

	@Test
	void contextLoads() {
	}

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
