package com.airesume.exportservice.consumer;

import com.airesume.exportservice.dto.ExportMessage;
import com.airesume.exportservice.service.ExportJobProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final ExportJobProcessor exportJobProcessor;

    @RabbitListener(queues = "q.pdf-exports")
    public void consumeExportMessage(ExportMessage message) {
        log.info("[RABBITMQ] Received export job task from queue for jobId: {}", message.getJobId());
        try {
            exportJobProcessor.processJob(message.getJobId(), message.getAuthorizationHeader());
            log.info("[RABBITMQ] Successfully processed and completed consumed export job: {}", message.getJobId());
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to process consumed export job for jobId: {}. Error: {}", 
                    message.getJobId(), e.getMessage(), e);
        }
    }
}
