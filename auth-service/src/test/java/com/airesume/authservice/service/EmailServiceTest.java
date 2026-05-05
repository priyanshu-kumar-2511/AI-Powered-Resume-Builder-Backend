package com.airesume.authservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for EmailService.
 * Technology: JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Test: Send OTP Email - Success")
    void sendOtpEmail_Success() {
        emailService.sendOtpEmail("test@example.com", "123456", "Password Reset");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Test: Send Username Recovery Email - Success")
    void sendUsernameEmail_Success() {
        emailService.sendUsernameEmail("test@example.com", "testuser");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Test: Send Welcome Login Email - Success")
    void sendWelcomeLoginEmail_Success() {
        emailService.sendWelcomeLoginEmail("test@example.com", "John Doe");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Test: Send Suspension Email - Success")
    void sendSuspensionEmail_Success() {
        emailService.sendSuspensionEmail("test@example.com", "John Doe", "Violation of Terms");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Test: Send Admin Promotion Email - Success")
    void sendAdminPromotionEmail_Success() {
        emailService.sendAdminPromotionEmail("test@example.com", "John Doe");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Test: Send Premium Activation Email - Success")
    void sendPremiumActivationEmail_Success() {
        emailService.sendPremiumActivationEmail("test@example.com", "John Doe");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Test: Send Admin Login Alert Email - Success")
    void sendAdminLoginAlertEmail_Success() {
        emailService.sendAdminLoginAlertEmail("test@example.com", "Admin User");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
