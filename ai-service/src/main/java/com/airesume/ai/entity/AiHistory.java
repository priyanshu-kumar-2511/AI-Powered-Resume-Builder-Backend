package com.airesume.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity representing the history of AI interactions.
 * Stores audit logs of prompts, responses, model details, and token usage 
 * for analytics and user history tracking.
 */
@Entity
@Table(name = "ai_history", indexes = {
    @Index(name = "idx_ai_history_userid", columnList = "userId")
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AiHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String actionType; // e.g., GENERATE_SUMMARY, CHECK_ATS

    @Column(columnDefinition = "TEXT")
    private String promptUsed;

    @Column(columnDefinition = "TEXT")
    private String responseContent;

    private String modelUsed;
    
    private Integer tokensUsed;

    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
