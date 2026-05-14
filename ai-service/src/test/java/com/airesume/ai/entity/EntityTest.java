package com.airesume.ai.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AI Service entities.
 * Verifies basic property mapping and lifecycle attributes for persistence models.
 */
class EntityTest {

    /**
     * Verifies the AI history entity data mapping.
     */
    @Test
    void testAiHistory() {
        AiHistory history = new AiHistory();
        history.setUserId("user123");
        history.setActionType("GENERATE");
        history.setPromptUsed("test prompt");
        history.setResponseContent("test response");

        assertEquals("user123", history.getUserId());
        assertEquals("GENERATE", history.getActionType());
        assertEquals("test prompt", history.getPromptUsed());
        assertEquals("test response", history.getResponseContent());
    }

    /**
     * Verifies the user quota management entity fields.
     */
    @Test
    void testUserQuota() {
        UserQuota quota = new UserQuota();
        quota.setUserId("user123");
        quota.setRemainingSummaryCount(10);
        quota.setRemainingAtsCount(5);
        quota.setPremium(true);

        assertEquals("user123", quota.getUserId());
        assertEquals(10, quota.getRemainingSummaryCount());
        assertEquals(5, quota.getRemainingAtsCount());
        assertTrue(quota.isPremium());
    }
}
