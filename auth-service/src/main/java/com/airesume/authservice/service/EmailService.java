package com.airesume.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service to handle email communications (OTPs, Recovery).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends a 6-digit OTP for account recovery.
     * Throws RuntimeException if mail sending fails so the error propagates to the client.
     */
    public void sendOtpEmail(String to, String otp, String flowType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Verification Code");
        message.setText("Hello,\n\nYour 6-digit verification code for " + flowType + " is: " + otp +
                        "\n\nThis code will expire in 10 minutes.\n\nRegards,\nResumeAI Team");

        try {
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Unable to send email. Please check your email address or try again later.");
        }
    }

    /**
     * Sends the recovered username to the user's email.
     * Throws RuntimeException if mail sending fails so the error propagates to the client.
     */
    public void sendUsernameEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Recovered Username");
        message.setText("Hello,\n\nYour username associated with this account is: " + username +
                        "\n\nYou can now use this to login to your dashboard.\n\nRegards,\nResumeAI Team");

        try {
            mailSender.send(message);
            log.info("Username recovery email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send Username email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Unable to send email. Please check your email address or try again later.");
        }
    }

    /**
     * Sends a welcome/thank-you email on every successful login.
     * Failure is logged but NOT thrown — login should never be blocked by a mail error.
     */
    public void sendWelcomeLoginEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome back to ResumeAI! 👋");
        message.setText(
            "Hello " + fullName + ",\n\n" +
            "Thank you for logging into ResumeAI!\n\n" +
            "We're glad to have you back. Here's what you can do today:\n" +
            "  📝  Build or update your resume with AI assistance\n" +
            "  🎨  Browse and apply professional resume templates\n" +
            "  🎯  Match your resume against live job postings\n\n" +
            "If this login wasn't you, please reset your password immediately:\n" +
            "  http://localhost:4200/forgot-password\n\n" +
            "Regards,\n" +
            "The ResumeAI Team"
        );

        try {
            mailSender.send(message);
            log.info("Welcome login email sent to: {}", to);
        } catch (Exception e) {
            // Non-critical — do not block login flow
            log.warn("Could not send welcome login email to {}: {}", to, e.getMessage());
        }
    }
}

