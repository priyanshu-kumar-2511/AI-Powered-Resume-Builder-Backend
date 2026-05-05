package com.airesume.adminserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "ADMIN_PASSWORD=secret",
    "ADMIN_USERNAME=admin"
})
class AdminServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
