package com.airesume.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a One-Time Password (OTP) for account recovery.
 * Used for both Forgot Username and Forgot Password flows.
 */
@Entity
@Table(name = "verification_otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VerificationOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String otpCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public enum OtpType {
        USERNAME_RECOVERY,
        PASSWORD_RESET,
        REGISTRATION
    }

    /**
     * Checks if the OTP has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
