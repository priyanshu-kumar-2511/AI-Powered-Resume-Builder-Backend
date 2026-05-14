package com.airesume.authservice.service;

import com.airesume.authservice.model.User;
import com.airesume.authservice.model.VerificationOtp;
import com.airesume.authservice.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the OTP (One-Time Password) Service.
 * Verifies secure generation, persistence, and validation of verification codes 
 * for account registration and password recovery.
 */
@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private OtpService otpService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();
    }

    /**
     * Verifies that a 6-digit OTP is generated and saved to the repository.
     */
    @Test
    void testGenerateAndSaveOtp() {
        String code = otpService.generateAndSaveOtp(testUser, VerificationOtp.OtpType.PASSWORD_RESET);

        assertNotNull(code);
        assertEquals(6, code.length());
        verify(otpRepository).deleteByUser(testUser);
        verify(otpRepository).save(any(VerificationOtp.class));
    }

    /**
     * Verifies successful validation of a valid, non-expired OTP.
     */
    @Test
    void testValidateOtp_Success() {
        VerificationOtp otp = VerificationOtp.builder()
                .otpCode("123456")
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        when(otpRepository.findByOtpCodeAndUserAndType("123456", testUser, VerificationOtp.OtpType.PASSWORD_RESET))
                .thenReturn(Optional.of(otp));

        boolean isValid = otpService.validateOtp(testUser, "123456", VerificationOtp.OtpType.PASSWORD_RESET);

        assertTrue(isValid);
    }

    /**
     * Ensures that an expired OTP is correctly identified as invalid.
     */
    @Test
    void testValidateOtp_Expired() {
        VerificationOtp otp = VerificationOtp.builder()
                .otpCode("123456")
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();

        when(otpRepository.findByOtpCodeAndUserAndType("123456", testUser, VerificationOtp.OtpType.PASSWORD_RESET))
                .thenReturn(Optional.of(otp));

        boolean isValid = otpService.validateOtp(testUser, "123456", VerificationOtp.OtpType.PASSWORD_RESET);

        assertFalse(isValid);
    }

    /**
     * Verifies that OTP validation returns false if no matching code is found in the database.
     */
    @Test
    void testValidateOtp_NotFound() {
        when(otpRepository.findByOtpCodeAndUserAndType(anyString(), any(), any()))
                .thenReturn(Optional.empty());

        boolean isValid = otpService.validateOtp(testUser, "000000", VerificationOtp.OtpType.PASSWORD_RESET);

        assertFalse(isValid);
    }
}
