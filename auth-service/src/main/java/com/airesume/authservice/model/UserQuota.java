package com.airesume.authservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity tracking the AI and feature quotas for a specific User.
 */
@Entity
@Table(name = "user_quotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private int aiCallsUsed = 0;

    @Builder.Default
    private int atsChecksUsed = 0;

    @Builder.Default
    private LocalDateTime lastResetDate = LocalDateTime.now();
}
