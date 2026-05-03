package com.airesume.exportservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.jwt.secret=VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdUZXN0U2VjcmV0S2V5MTIzNDU2Nzg5MA=="
})
class ExportServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
