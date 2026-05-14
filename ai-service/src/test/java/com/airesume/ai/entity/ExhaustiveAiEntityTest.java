package com.airesume.ai.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Exhaustive unit tests for AI Service entities.
 * Verifies detailed equality logic, hashcode stability, and JPA lifecycle callbacks 
 * for persistence models.
 */
class ExhaustiveAiEntityTest {

    /**
     * Verifies all fields of the AI history entity, including deep equality and hashcode branches.
     */
    @Test
    void testAiHistoryExhaustive() {
        LocalDateTime now = LocalDateTime.now();
        AiHistory h1 = AiHistory.builder()
                .id(1L)
                .userId("u1")
                .actionType("GEN")
                .promptUsed("p")
                .responseContent("r")
                .modelUsed("m")
                .createdAt(now)
                .build();

        AiHistory h2 = h1.toBuilder().build();

        assertEquals(h1, h2);
        assertEquals(h1.hashCode(), h2.hashCode());
        assertNotNull(h1.toString());

        // Test individual field changes for equals/hashCode branches
        assertNotEquals(h1, h1.toBuilder().id(2L).build());
        assertNotEquals(h1, h1.toBuilder().userId("u2").build());
        assertNotEquals(h1, h1.toBuilder().actionType("GEN2").build());
        assertNotEquals(h1, h1.toBuilder().promptUsed("p2").build());
        assertNotEquals(h1, h1.toBuilder().responseContent("r2").build());
        assertNotEquals(h1, h1.toBuilder().modelUsed("m2").build());
        assertNotEquals(h1, h1.toBuilder().createdAt(now.plusDays(1)).build());

        // Null checks for branches
        assertNotEquals(h1, h1.toBuilder().id(null).build());
        assertNotEquals(h1, new Object());
        assertNotEquals(h1, (AiHistory) null);
        assertEquals(h1, h1);
    }

    /**
     * Verifies all fields of the user quota entity and JPA lifecycle (@PrePersist) callbacks.
     */
    @Test
    void testUserQuotaExhaustive() {
        LocalDateTime now = LocalDateTime.now();
        UserQuota q1 = UserQuota.builder()
                .userId("u1")
                .remainingSummaryCount(10)
                .remainingAtsCount(5)
                .isPremium(true)
                .lastResetDate(now)
                .build();

        UserQuota q2 = q1.toBuilder().build();

        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
        assertNotNull(q1.toString());

        // Test individual field changes
        assertNotEquals(q1, q1.toBuilder().userId("u2").build());
        assertNotEquals(q1, q1.toBuilder().remainingSummaryCount(9).build());
        assertNotEquals(q1, q1.toBuilder().remainingAtsCount(4).build());
        assertNotEquals(q1, q1.toBuilder().isPremium(false).build());
        assertNotEquals(q1, q1.toBuilder().lastResetDate(now.plusDays(1)).build());

        // Coverage for PrePersist
        UserQuota q3 = new UserQuota();
        q3.onCreate();
        assertNotNull(q3.getLastResetDate());
        
        // Coverage for PrePersist when lastResetDate is already set
        LocalDateTime customTime = LocalDateTime.now().minusDays(5);
        UserQuota q4 = UserQuota.builder().lastResetDate(customTime).build();
        q4.onCreate();
        assertEquals(customTime, q4.getLastResetDate());
        
        AiHistory h3 = new AiHistory();
        h3.onCreate();
        assertNotNull(h3.getCreatedAt());

        // Coverage for PrePersist when createdAt is already set (it gets unconditionally overwritten)
        LocalDateTime customCreated = LocalDateTime.now().minusDays(5);
        AiHistory h4 = AiHistory.builder().createdAt(customCreated).build();
        h4.onCreate();
        assertNotEquals(customCreated, h4.getCreatedAt());
        assertNotNull(h4.getCreatedAt());
    }
}
