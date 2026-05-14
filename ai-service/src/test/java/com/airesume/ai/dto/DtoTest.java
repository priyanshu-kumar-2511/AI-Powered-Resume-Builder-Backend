package com.airesume.ai.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AI Service Data Transfer Objects (DTOs).
 * Verifies correct data mapping, builder functionality, and property accessors.
 */
class DtoTest {

    /**
     * Verifies the AI generation request payload and builder.
     */
    @Test
    void testAiRequest() {
        AiRequest req = AiRequest.builder()
                .userId("user123")
                .resumeId(1L)
                .targetJobTitle("Developer")
                .jobDescription("Java dev")
                .existingContent("Some content")
                .existingBullets(List.of("Bullet 1"))
                .language("English")
                .targetLanguage("Spanish")
                .sectionType("Summary")
                .tone("Professional")
                .build();

        assertEquals("user123", req.getUserId());
        assertEquals(1L, req.getResumeId());
        assertEquals("Developer", req.getTargetJobTitle());
        assertEquals("Java dev", req.getJobDescription());
        assertEquals("Some content", req.getExistingContent());
        assertEquals(1, req.getExistingBullets().size());
        assertEquals("English", req.getLanguage());
        assertEquals("Spanish", req.getTargetLanguage());
        assertEquals("Summary", req.getSectionType());
        assertEquals("Professional", req.getTone());

        AiRequest req2 = new AiRequest();
        req2.setUserId("user456");
        assertEquals("user456", req2.getUserId());
    }

    /**
     * Verifies the AI generation response data structure.
     */
    @Test
    void testAiResponse() {
        AiResponse resp = new AiResponse("content", List.of("b1"), 80, Map.of("key", "val"));
        assertEquals("content", resp.getContent());
        assertEquals(1, resp.getBulletPoints().size());
        assertEquals(80, resp.getScore());
        assertEquals("val", resp.getMetadata().get("key"));

        AiResponse resp2 = new AiResponse();
        resp2.setContent("c2");
        assertEquals("c2", resp2.getContent());
    }
}
