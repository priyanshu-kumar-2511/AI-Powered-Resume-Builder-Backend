package com.airesume.notificationservice.consumer;

import com.airesume.notificationservice.dto.EmailMessage;
import com.airesume.notificationservice.dto.EmailRequest;
import com.airesume.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "q.emails")
    public void consumeEmailMessage(EmailMessage message) {
        log.info("[RABBITMQ] Received email message task for recipient: {}", message.getTo());
        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(message.getTo());
            emailRequest.setSubject(message.getSubject());
            emailRequest.setBody(message.getBody());
            emailRequest.setHtml(message.isHtml());

            emailService.sendEmail(emailRequest);
            log.info("[RABBITMQ] Successfully dispatched email via SMTP for recipient: {}", message.getTo());
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to process consumed email message task for recipient: {}. Error: {}", 
                    message.getTo(), e.getMessage(), e);
        }
    }
}
