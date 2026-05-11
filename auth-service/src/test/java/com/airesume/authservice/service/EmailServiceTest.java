package com.airesume.authservice.service;

import com.airesume.authservice.dto.EmailMessage;
import com.airesume.authservice.config.RabbitMQConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for EmailService.
 * Technology: JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Test: Send OTP Email - Success")
    void sendOtpEmail_Success() {
        emailService.sendOtpEmail("test@example.com", "123456", "Password Reset");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Username Recovery Email - Success")
    void sendUsernameEmail_Success() {
        emailService.sendUsernameEmail("test@example.com", "testuser");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Welcome Login Email - Success")
    void sendWelcomeLoginEmail_Success() {
        emailService.sendWelcomeLoginEmail("test@example.com", "John Doe");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Suspension Email - Success")
    void sendSuspensionEmail_Success() {
        emailService.sendSuspensionEmail("test@example.com", "John Doe", "Violation of Terms");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Admin Promotion Email - Success")
    void sendAdminPromotionEmail_Success() {
        emailService.sendAdminPromotionEmail("test@example.com", "John Doe");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Premium Activation Email - Success")
    void sendPremiumActivationEmail_Success() {
        emailService.sendPremiumActivationEmail("test@example.com", "John Doe");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Admin Login Alert Email - Success")
    void sendAdminLoginAlertEmail_Success() {
        emailService.sendAdminLoginAlertEmail("test@example.com", "Admin User");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Reactivation Email")
    void sendReactivationEmail_Success() {
        emailService.sendReactivationEmail("test@example.com", "John");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Admin Demotion Email")
    void sendAdminDemotionEmail_Success() {
        emailService.sendAdminDemotionEmail("test@example.com", "John");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Send Premium Cancellation Email")
    void sendPremiumCancellationEmail_Success() {
        emailService.sendPremiumCancellationEmail("test@example.com", "John");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Exception Handling - sendOtpEmail")
    void sendOtpEmail_Exception() {
        doThrow(new RuntimeException("Queue broker down")).when(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
        // Should not throw exception because exceptions are caught and logged inside publishEmail
        emailService.sendOtpEmail("test@example.com", "123", "flow");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("Test: Exception Handling - sendWelcomeLoginEmail")
    void sendWelcomeLoginEmail_Exception() {
        doThrow(new RuntimeException("Queue broker down")).when(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
        // Should not throw exception
        emailService.sendWelcomeLoginEmail("test@example.com", "John");
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }
}
