package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.BulkNotificationRequest;
import com.airesume.notificationservice.dto.NotificationRequest;
import com.airesume.notificationservice.dto.NotificationResponse;
import com.airesume.notificationservice.dto.AdminUserDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AdminUserClient adminUserClient;

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
    public void sendBulkNotification(BulkNotificationRequest request, String authorizationHeader) {
        List<AdminUserDto> users = adminUserClient.getAllUsers(authorizationHeader);
        List<Notification> notifications = new ArrayList<>();

        for (AdminUserDto user : users) {
            if (!user.isActive() || user.getUserId() == null) {
                continue;
            }
            if (!matchesTier(request.getTier(), user.getSubscriptionPlan())) {
                continue;
            }

            notifications.add(Notification.builder()
                    .recipientId(user.getUserId())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(request.getType())
                    .tier(request.getTier())
                    .isRead(false)
                    .build());
        }

        if (notifications.isEmpty()) {
            log.warn("No recipients found for bulk notification '{}' with tier {}", request.getTitle(), request.getTier());
            return;
        }

        notificationRepository.saveAll(notifications);
        log.info("Bulk notification broadcasted: {} for Tier: {} to {} users",
                request.getTitle(), request.getTier(), notifications.size());
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

    private boolean matchesTier(NotificationTier requestedTier, String userPlan) {
        if (requestedTier == null || requestedTier == NotificationTier.ALL) {
            return true;
        }
        if (userPlan == null || userPlan.isBlank()) {
            return false;
        }
        return requestedTier.name().equalsIgnoreCase(userPlan.trim().toUpperCase(Locale.ROOT));
    }
}
