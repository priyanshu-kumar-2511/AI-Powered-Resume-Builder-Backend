package com.airesume.ai.consumer;

import com.airesume.ai.dto.AiJobMessage;
import com.airesume.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final AiService aiService;

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
