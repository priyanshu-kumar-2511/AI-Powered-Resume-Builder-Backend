package com.airesume.resumeservice;

import com.airesume.resumeservice.client.SectionServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ResumeServiceApplicationTests {

    @MockitoBean
    private SectionServiceClient sectionServiceClient;

    @Test
    void contextLoads() {
        assertTrue(true, "Spring context loaded successfully");
    }

    @Test
    void testMain() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            ResumeServiceApplication.main(new String[]{"--server.port=0"});
        });
    }
}
