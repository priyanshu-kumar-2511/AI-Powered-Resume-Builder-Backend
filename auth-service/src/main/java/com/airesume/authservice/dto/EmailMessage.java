package com.airesume.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing an Email Message.
 * Used for publishing email tasks to the message broker (RabbitMQ)
 * to be consumed by the notification service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String to;
    private String subject;
    private String body;
    private boolean isHtml;
}
