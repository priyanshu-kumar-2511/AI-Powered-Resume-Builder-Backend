package com.airesume.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_quotas")
@Data
@Builder
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
