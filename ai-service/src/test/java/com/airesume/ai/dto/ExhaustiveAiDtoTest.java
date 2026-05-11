package com.airesume.ai.dto;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ExhaustiveAiDtoTest {

    @Test
    void testAiRequestExhaustive() {
        AiRequest r1 = AiRequest.builder()
                .userId("u1")
                .resumeId(1L)
                .targetJobTitle("t")
                .jobDescription("d")
                .existingContent("e")
                .existingBullets(Collections.singletonList("b"))
                .language("l")
                .targetLanguage("tl")
                .sectionType("s")
                .tone("professional")
                .build();

        AiRequest r2 = r1.toBuilder().build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());

        // Branches
        assertNotEquals(r1, r1.toBuilder().userId("u2").build());
        assertNotEquals(r1, r1.toBuilder().resumeId(2L).build());
        assertNotEquals(r1, r1.toBuilder().targetJobTitle("t2").build());
        assertNotEquals(r1, r1.toBuilder().jobDescription("d2").build());
        assertNotEquals(r1, r1.toBuilder().existingContent("e2").build());
        assertNotEquals(r1, r1.toBuilder().existingBullets(Collections.emptyList()).build());
        assertNotEquals(r1, r1.toBuilder().language("l2").build());
        assertNotEquals(r1, r1.toBuilder().targetLanguage("tl2").build());
        assertNotEquals(r1, r1.toBuilder().sectionType("s2").build());
        assertNotEquals(r1, r1.toBuilder().tone("tone2").build());
    }

    @Test
    void testAiResponseExhaustive() {
        AiResponse r1 = AiResponse.builder()
                .content("c")
                .bulletPoints(Collections.singletonList("b"))
                .score(80)
                .metadata(Map.of("k", "v"))
                .build();

        AiResponse r2 = r1.toBuilder().build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());

        // Branches
        assertNotEquals(r1, r1.toBuilder().content("c2").build());
        assertNotEquals(r1, r1.toBuilder().bulletPoints(Collections.emptyList()).build());
        assertNotEquals(r1, r1.toBuilder().score(90).build());
        assertNotEquals(r1, r1.toBuilder().metadata(Collections.emptyMap()).build());
    }
}
