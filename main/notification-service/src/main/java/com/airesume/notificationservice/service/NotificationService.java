package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.BulkNotificationRequest;
import com.airesume.notificationservice.dto.NotificationRequest;
import com.airesume.notificationservice.dto.NotificationResponse;
import com.airesume.notificationservice.model.Notification;
import com.airesume.notificationservice.model.NotificationTier;
import com.airesume.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .tier(NotificationTier.ALL)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        log.info("Sent notification ID {} to user {}", notification.getId(), request.getRecipientId());
        return mapToResponse(notification);
    }

    @Transactional
    public void sendBulkNotification(BulkNotificationRequest request) {
        // In a real microservice architecture, this would query Auth/User Service
        // for all users matching the requested tier and save notifications for each.
        // For the scope of this API, we will just log the intent or save a global notification.
        // Assuming global notifications will be fetched by a different logic or we use event-driven 
        // to populate individual inboxes.
        log.info("Bulk notification broadcasted: {} for Tier: {}", request.getTitle(), request.getTier());
    }

    public Page<NotificationResponse> getNotificationsForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id " + notificationId));
        
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadList = notificationRepository.findByRecipientIdAndIsReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();
        unreadList.forEach(n -> {
            n.setRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unreadList);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .tier(notification.getTier())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
