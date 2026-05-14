package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Email Delivery Service.
 * Verifies MimeMessage creation, SMTP configuration handling, and 
 * robust error reporting for mail server failures.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "from@example.com");
    }

    /**
     * Verifies successful email dispatch using standard MimeMessage.
     */
    @Test
    void testSendEmail_Success() {
        EmailRequest request = new EmailRequest();
        request.setTo("to@example.com");
        request.setSubject("Subject");
        request.setBody("Body");
        request.setHtml(false);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(request);

        verify(mailSender).send(any(MimeMessage.class));
    }

    /**
     * Ensures that exceptions from the mail sender are correctly wrapped and rethrown.
     */
    @Test
    void testSendEmail_Failure() {
        EmailRequest request = new EmailRequest();
        request.setTo("to@example.com");
        
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertThrows(RuntimeException.class, () -> emailService.sendEmail(request));
    }

    /**
     * Verifies that messaging-level exceptions (e.g., invalid headers) are caught and wrapped in a RuntimeException.
     */
    @Test
    void testSendEmail_MessagingException() throws jakarta.mail.MessagingException {
        EmailRequest request = new EmailRequest();
        request.setTo("to@example.com");
        request.setSubject("Subject");
        request.setBody("Body");
        request.setHtml(false);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new jakarta.mail.MessagingException("Invalid Subject")).when(mimeMessage)
                .setSubject(anyString(), anyString());

        assertThrows(RuntimeException.class, () -> emailService.sendEmail(request));
    }
}
