package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service responsible for the physical delivery of email messages.
 * Uses JavaMailSender to dispatch emails via SMTP (configured via properties).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Dispatches an email message using the provided request details.
     * Supports both plain text and HTML content.
     * 
     * @param request the email details including recipient, subject, and body
     * @throws RuntimeException if email delivery fails
     */
    public void sendEmail(EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());

            mailSender.send(message);
            log.info("Email sent successfully to: {}", request.getTo());
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", request.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
