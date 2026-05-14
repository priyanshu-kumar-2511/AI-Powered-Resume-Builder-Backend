package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.*;
import com.airesume.notificationservice.model.Notification;
import com.airesume.notificationservice.model.NotificationTier;
import com.airesume.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Notification Service.
 * Covers direct notification delivery, bulk tier-based broadcasting,
 * and read/unread status management.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AdminUserClient adminUserClient;

    @InjectMocks
    private NotificationService notificationService;

    /**
     * Verifies that a single notification is correctly saved and returned.
     */
    @Test
    void testSendNotification() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(1L);
        request.setTitle("Test");
        request.setMessage("Body");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals("Test", response.getTitle());
        verify(notificationRepository).save(any());
    }

    /**
     * Tests the bulk notification flow, ensuring the Admin user client is called.
     */
    @Test
    void testSendBulkNotification() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTitle("Bulk");
        request.setTier(NotificationTier.PREMIUM);

        AdminUserDto user = new AdminUserDto();
        user.setUserId(1L);
        user.setSubscriptionPlan("PREMIUM");
        user.setActive(true);

        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.singletonList(user));

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository).saveAll(anyList());
    }

    /**
     * Verifies that bulk notification logic handles empty user lists gracefully.
     */
    @Test
    void testSendBulkNotification_NoUsers() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.emptyList());

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository, never()).saveAll(anyList());
    }

    /**
     * Verifies that inactive users are excluded from bulk notification delivery.
     */
    @Test
    void testSendBulkNotification_InactiveUser() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTier(NotificationTier.ALL);
        
        AdminUserDto user = new AdminUserDto();
        user.setActive(false);
        user.setUserId(1L);

        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.singletonList(user));

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository, never()).saveAll(anyList());
    }

    /**
     * Verifies that users are excluded from bulk delivery if their subscription tier doesn't match the target tier.
     */
    @Test
    void testSendBulkNotification_TierMismatch() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTier(NotificationTier.PREMIUM);
        
        AdminUserDto user = new AdminUserDto();
        user.setActive(true);
        user.setUserId(1L);
        user.setSubscriptionPlan("FREE");

        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.singletonList(user));

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository, never()).saveAll(anyList());
    }

    /**
     * Verifies the count of unread notifications for a recipient.
     */
    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(10L);
        assertEquals(10L, notificationService.getUnreadCount(1L));
    }

    /**
     * Tests marking a specific notification as read.
     */
    @Test
    void testMarkAsRead() {
        Notification notification = Notification.builder().id(1L).isRead(false).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    /**
     * Verifies that attempting to mark a non-existent notification as read throws a RuntimeException.
     */
    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(1L));
    }

    /**
     * Verifies that marking an already read notification as read doesn't trigger unnecessary database updates.
     */
    @Test
    void testMarkAsRead_AlreadyRead() {
        Notification notification = Notification.builder().id(1L).isRead(true).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        verify(notificationRepository, never()).save(any());
    }

    /**
     * Verifies that all unread notifications for a user can be marked as read in a single operation.
     */
    @Test
    void testMarkAllAsRead() {
        Notification n1 = Notification.builder().isRead(false).build();
        when(notificationRepository.findByRecipientIdAndIsReadFalse(1L)).thenReturn(Collections.singletonList(n1));

        notificationService.markAllAsRead(1L);

        assertTrue(n1.isRead());
        verify(notificationRepository).saveAll(anyList());
    }

    /**
     * Verifies that a notification can be permanently deleted by its ID.
     */
    @Test
    void testDeleteNotification() {
        notificationService.deleteNotification(1L);
        verify(notificationRepository).deleteById(1L);
    }

    /**
     * Verifies administrative retrieval of all notifications with pagination support.
     */
    @Test
    void testGetAllNotifications() {
        when(notificationRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(org.springframework.data.domain.Page.empty());
        assertNotNull(notificationService.getAllNotifications(org.springframework.data.domain.PageRequest.of(0, 10)));
    }

    /**
     * Verifies paginated retrieval of notifications for a specific user, ordered by creation date.
     */
    @Test
    void testGetNotificationsForUser() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(eq(1L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        assertNotNull(notificationService.getNotificationsForUser(1L, org.springframework.data.domain.PageRequest.of(0, 10)));
    }

    /**
     * Verifies that bulk delivery logic skips users with missing IDs.
     */
    @Test
    void testSendBulkNotification_UserIdNull() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTier(NotificationTier.ALL);
        
        AdminUserDto user = new AdminUserDto();
        user.setActive(true);
        user.setUserId(null);

        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.singletonList(user));

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository, never()).saveAll(anyList());
    }

    /**
     * Verifies that bulk delivery defaults to broadcasting to everyone if no specific tier is specified.
     */
    @Test
    void testSendBulkNotification_NullTier() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTier(null);
        
        AdminUserDto user = new AdminUserDto();
        user.setActive(true);
        user.setUserId(1L);
        user.setSubscriptionPlan("PREMIUM");

        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.singletonList(user));

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository).saveAll(anyList());
    }

    /**
     * Verifies that bulk delivery skips users with missing subscription plan metadata.
     */
    @Test
    void testSendBulkNotification_UserPlanNull() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTier(NotificationTier.PREMIUM);
        
        AdminUserDto user = new AdminUserDto();
        user.setActive(true);
        user.setUserId(1L);
        user.setSubscriptionPlan(null);

        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.singletonList(user));

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository, never()).saveAll(anyList());
    }

    /**
     * Verifies that bulk delivery skips users with blank or whitespace-only subscription plans.
     */
    @Test
    void testSendBulkNotification_UserPlanBlank() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTier(NotificationTier.PREMIUM);
        
        AdminUserDto user = new AdminUserDto();
        user.setActive(true);
        user.setUserId(1L);
        user.setSubscriptionPlan("   ");

        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.singletonList(user));

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository, never()).saveAll(anyList());
    }
}
