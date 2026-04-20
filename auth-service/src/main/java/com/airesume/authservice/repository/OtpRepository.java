package com.airesume.authservice.repository;

import com.airesume.authservice.model.User;
import com.airesume.authservice.model.VerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for VerificationOtp entity operations.
 */
public interface OtpRepository extends JpaRepository<VerificationOtp, Long> {
    Optional<VerificationOtp> findByOtpCodeAndUserAndType(String otpCode, User user, VerificationOtp.OtpType type);
    void deleteByUser(User user);
}
