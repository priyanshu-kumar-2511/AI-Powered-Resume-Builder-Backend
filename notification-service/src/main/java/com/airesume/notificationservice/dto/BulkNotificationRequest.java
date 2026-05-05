package com.airesume.notificationservice.dto;

import com.airesume.notificationservice.model.NotificationTier;
import com.airesume.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkNotificationRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private NotificationType type = NotificationType.INFO;

    @NotNull(message = "Tier is required (e.g., ALL, FREE, PRO, PREMIUM)")
    private NotificationTier tier;
}
