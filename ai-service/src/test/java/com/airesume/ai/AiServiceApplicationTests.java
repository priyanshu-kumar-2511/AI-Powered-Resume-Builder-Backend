package com.airesume.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AiServiceApplicationTests {

    @Test
    void contextLoads() {
        // Basic test to ensure context starts up and covers the Application class
    }
    
    @Test
    void mainMethodTest() {
        // Covering the main method
        AiServiceApplication.main(new String[]{"--spring.profiles.active=test"});
    }
}
