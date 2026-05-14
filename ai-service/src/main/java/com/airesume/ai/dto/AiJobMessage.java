package com.airesume.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for asynchronous AI job messages.
 * Published to RabbitMQ for background processing of intensive tasks 
 * like full resume tailoring and translations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiJobMessage {
    private String userId;
    private String actionType;
    private String promptText;
}
