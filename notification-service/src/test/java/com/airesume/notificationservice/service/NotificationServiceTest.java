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

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AdminUserClient adminUserClient;

    @InjectMocks
    private NotificationService notificationService;

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

    @Test
    void testSendBulkNotification_NoUsers() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        when(adminUserClient.getAllUsers(anyString())).thenReturn(Collections.emptyList());

        notificationService.sendBulkNotification(request, "Bearer token");

        verify(notificationRepository, never()).saveAll(anyList());
    }

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

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(10L);
        assertEquals(10L, notificationService.getUnreadCount(1L));
    }

    @Test
    void testMarkAsRead() {
        Notification notification = Notification.builder().id(1L).isRead(false).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(1L));
    }

    @Test
    void testMarkAsRead_AlreadyRead() {
        Notification notification = Notification.builder().id(1L).isRead(true).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAllAsRead() {
        Notification n1 = Notification.builder().isRead(false).build();
        when(notificationRepository.findByRecipientIdAndIsReadFalse(1L)).thenReturn(Collections.singletonList(n1));

        notificationService.markAllAsRead(1L);

        assertTrue(n1.isRead());
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void testDeleteNotification() {
        notificationService.deleteNotification(1L);
        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void testGetAllNotifications() {
        when(notificationRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(org.springframework.data.domain.Page.empty());
        assertNotNull(notificationService.getAllNotifications(org.springframework.data.domain.PageRequest.of(0, 10)));
    }

    @Test
    void testGetNotificationsForUser() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(eq(1L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        assertNotNull(notificationService.getNotificationsForUser(1L, org.springframework.data.domain.PageRequest.of(0, 10)));
    }

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
