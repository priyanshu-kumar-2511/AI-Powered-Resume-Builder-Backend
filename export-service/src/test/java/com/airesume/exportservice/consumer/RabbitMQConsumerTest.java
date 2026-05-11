package com.airesume.exportservice.consumer;

import com.airesume.exportservice.dto.ExportMessage;
import com.airesume.exportservice.service.ExportJobProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @Mock
    private ExportJobProcessor exportJobProcessor;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    @Test
    void testConsumeExportMessage_Success() {
        ExportMessage message = new ExportMessage();
        message.setJobId("job-123");
        message.setAuthorizationHeader("Bearer token");

        rabbitMQConsumer.consumeExportMessage(message);

        verify(exportJobProcessor, times(1)).processJob("job-123", "Bearer token");
    }

    @Test
    void testConsumeExportMessage_Failure() {
        ExportMessage message = new ExportMessage();
        message.setJobId("job-123");
        message.setAuthorizationHeader("Bearer token");

        doThrow(new RuntimeException("Processing error")).when(exportJobProcessor).processJob(anyString(), anyString());

        rabbitMQConsumer.consumeExportMessage(message);

        verify(exportJobProcessor, times(1)).processJob("job-123", "Bearer token");
    }
}
