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
        }
    }

    /**
     * Sends the recovered username to the user's email.
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
        }
    }
}
