package com.airesume.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity tracking AI usage quotas per user.
 * Manages limits for specific AI actions like Summary generation and ATS checks,
 * differentiating between Free and Premium tiers.
 */
@Entity
@Table(name = "user_quotas")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserQuota {
    @Id
    private String userId;

    private int remainingSummaryCount;
    private int remainingAtsCount;

    private boolean isPremium;

    private LocalDateTime lastResetDate;

    @PrePersist
    protected void onCreate() {
        if (lastResetDate == null) {
            lastResetDate = LocalDateTime.now();
        }
    }
}
