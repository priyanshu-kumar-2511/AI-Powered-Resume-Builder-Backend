package com.airesume.ai.consumer;

import com.airesume.ai.dto.AiJobMessage;
import com.airesume.ai.service.AiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the RabbitMQ Message Consumer.
 * Verifies that messages received from the AI job queue are correctly
 * dispatched to the background processing service.
 */
@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @Mock
    private AiService aiService;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    /**
     * Verifies that a valid AI job message is correctly consumed and processed.
     */
    @Test
    void testConsumeAiJob_Success() {
        AiJobMessage message = AiJobMessage.builder()
                .userId("user1")
                .actionType("TAILOR_RESUME")
                .promptText("Please tailor my resume")
                .build();

        rabbitMQConsumer.consumeAiJob(message);

        verify(aiService).processBackgroundAiJob("Please tailor my resume", "user1", "TAILOR_RESUME");
    }

    /**
     * Ensures that service failures during message processing are gracefully handled.
     */
    @Test
    void testConsumeAiJob_Failure() {
        AiJobMessage message = AiJobMessage.builder()
                .userId("user1")
                .actionType("TAILOR_RESUME")
                .promptText("Please tailor my resume")
                .build();

        doThrow(new RuntimeException("Service failure"))
                .when(aiService).processBackgroundAiJob(anyString(), anyString(), anyString());

        // Should not throw exception as it's caught and logged
        rabbitMQConsumer.consumeAiJob(message);

        verify(aiService).processBackgroundAiJob(anyString(), anyString(), anyString());
    }
}
