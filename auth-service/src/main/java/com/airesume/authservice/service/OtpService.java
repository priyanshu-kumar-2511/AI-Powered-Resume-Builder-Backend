package com.airesume.authservice.service;

import com.airesume.authservice.model.User;
import com.airesume.authservice.model.VerificationOtp;
import com.airesume.authservice.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service to manage 6-digit OTP generation and validation.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final Random random = new Random();

    /**
     * Generates a new 6-digit numeric OTP and saves it to the database.
     * Cleans up any existing OTPs for the user before saving the new one.
     * 
     * @param user the user for whom the OTP is being generated
     * @param type the purpose of the OTP (REGISTRATION, RECOVERY, etc.)
     * @return the generated 6-digit code
     */
    @Transactional
    public String generateAndSaveOtp(User user, VerificationOtp.OtpType type) {
        // Remove existing OTPs for this user to keep it clean
        otpRepository.deleteByUser(user);

        String code = String.format("%06d", random.nextInt(1000000));
        
        VerificationOtp otp = VerificationOtp.builder()
                .otpCode(code)
                .type(type)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(otp);
        return code;
    }

    /**
     * Validates if the provided OTP is correct and hasn't expired.
     * 
     * @param user the user whose OTP is being validated
     * @param code the code provided by the user
     * @param type the expected type of the OTP
     * @return true if the OTP is valid and active, false otherwise
     */
    public boolean validateOtp(User user, String code, VerificationOtp.OtpType type) {
        return otpRepository.findByOtpCodeAndUserAndType(code, user, type)
                .map(otp -> !otp.isExpired())
                .orElse(false);
    }
}
