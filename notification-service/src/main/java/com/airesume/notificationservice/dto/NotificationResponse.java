package com.airesume.notificationservice.dto;

import com.airesume.notificationservice.model.NotificationTier;
import com.airesume.notificationservice.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long recipientId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationTier tier;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
