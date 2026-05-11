package com.airesume.notificationservice.consumer;

import com.airesume.notificationservice.dto.EmailMessage;
import com.airesume.notificationservice.dto.EmailRequest;
import com.airesume.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    @Test
    void testConsumeEmailMessage_Success() {
        EmailMessage message = new EmailMessage();
        message.setTo("test@example.com");
        message.setSubject("Subject");
        message.setBody("Body");
        message.setHtml(true);

        rabbitMQConsumer.consumeEmailMessage(message);

        verify(emailService, times(1)).sendEmail(any(EmailRequest.class));
    }

    @Test
    void testConsumeEmailMessage_Failure() {
        EmailMessage message = new EmailMessage();
        message.setTo("test@example.com");
        message.setSubject("Subject");
        message.setBody("Body");
        message.setHtml(true);

        doThrow(new RuntimeException("SMTP error")).when(emailService).sendEmail(any(EmailRequest.class));

        rabbitMQConsumer.consumeEmailMessage(message);

        verify(emailService, times(1)).sendEmail(any(EmailRequest.class));
    }
}
