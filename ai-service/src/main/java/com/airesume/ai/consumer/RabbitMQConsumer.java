package com.airesume.ai.consumer;

import com.airesume.ai.dto.AiJobMessage;
import com.airesume.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for RabbitMQ messages.
 * Listens for background AI jobs (e.g., long-running resume analysis or content generation)
 * and delegates them to the AiService for processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final AiService aiService;

    /**
     * Listens to the 'q.ai-jobs' queue and processes incoming AI task messages.
     * Includes error handling to ensure queue stability.
     */
    @RabbitListener(queues = "q.ai-jobs")
    public void consumeAiJob(AiJobMessage message) {
        log.info("[RABBITMQ] Received background AI task for user: {}, actionType: {}", 
                message.getUserId(), message.getActionType());
        try {
            aiService.processBackgroundAiJob(message.getPromptText(), message.getUserId(), message.getActionType());
            log.info("[RABBITMQ] Successfully executed background AI task for user: {}, actionType: {}", 
                    message.getUserId(), message.getActionType());
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to process consumed background AI task: {}", e.getMessage(), e);
        }
    }
}
