package com.airesume.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true, "Spring context loaded successfully");
    }

    @Test
    void testMain() {
        assertDoesNotThrow(() -> {
            AuthServiceApplication.main(new String[]{"--server.port=0"});
        });
    }
}
