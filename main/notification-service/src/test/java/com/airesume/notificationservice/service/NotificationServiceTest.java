package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.BulkNotificationRequest;
import com.airesume.notificationservice.dto.NotificationRequest;
import com.airesume.notificationservice.dto.NotificationResponse;
import com.airesume.notificationservice.model.Notification;
import com.airesume.notificationservice.model.NotificationTier;
import com.airesume.notificationservice.model.NotificationType;
import com.airesume.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest sampleRequest;
    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        sampleRequest = new NotificationRequest();
        sampleRequest.setRecipientId(101L);
        sampleRequest.setTitle("Welcome!");
        sampleRequest.setMessage("Thanks for joining ResumeAI.");
        sampleRequest.setType(NotificationType.INFO);

        savedNotification = Notification.builder()
                .id(1L)
                .recipientId(101L)
                .title("Welcome!")
                .message("Thanks for joining ResumeAI.")
                .type(NotificationType.INFO)
                .tier(NotificationTier.ALL)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testSendNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        NotificationResponse response = notificationService.sendNotification(sampleRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getRecipientId());
        assertEquals("Welcome!", response.getTitle());
        assertFalse(response.isRead());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendBulkNotification_Success() {
        BulkNotificationRequest bulkRequest = new BulkNotificationRequest();
        bulkRequest.setTitle("System Update");
        bulkRequest.setMessage("Maintenance tonight.");
        bulkRequest.setTier(NotificationTier.ALL);
        bulkRequest.setType(NotificationType.SYSTEM);

        assertDoesNotThrow(() -> notificationService.sendBulkNotification(bulkRequest));
    }

    @Test
    void testGetNotificationsForUser_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> mockPage = new PageImpl<>(List.of(savedNotification));
        
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(101L, pageable)).thenReturn(mockPage);

        Page<NotificationResponse> result = notificationService.getNotificationsForUser(101L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Welcome!", result.getContent().get(0).getTitle());
        
        verify(notificationRepository, times(1)).findByRecipientIdOrderByCreatedAtDesc(101L, pageable);
    }

    @Test
    void testGetUnreadCount_Success() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(101L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(101L);

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByRecipientIdAndIsReadFalse(101L);
    }

    @Test
    void testMarkAsRead_UnreadNotification_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.markAsRead(1L);

        assertTrue(savedNotification.isRead());
        assertNotNull(savedNotification.getReadAt());
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(savedNotification);
    }

    @Test
    void testMarkAsRead_AlreadyRead_DoesNothing() {
        savedNotification.setRead(true);
        savedNotification.setReadAt(LocalDateTime.now());
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));

        notificationService.markAsRead(1L);

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationService.markAsRead(1L));
        assertEquals("Notification not found with id 1", exception.getMessage());
        
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    void testMarkAllAsRead_Success() {
        Notification n1 = Notification.builder().id(1L).isRead(false).build();
        Notification n2 = Notification.builder().id(2L).isRead(false).build();
        List<Notification> unreadList = Arrays.asList(n1, n2);

        when(notificationRepository.findByRecipientIdAndIsReadFalse(101L)).thenReturn(unreadList);
        when(notificationRepository.saveAll(unreadList)).thenReturn(unreadList);

        notificationService.markAllAsRead(101L);

        assertTrue(n1.isRead());
        assertNotNull(n1.getReadAt());
        assertTrue(n2.isRead());
        assertNotNull(n2.getReadAt());
        
        verify(notificationRepository, times(1)).findByRecipientIdAndIsReadFalse(101L);
        verify(notificationRepository, times(1)).saveAll(unreadList);
    }

    @Test
    void testDeleteNotification_Success() {
        doNothing().when(notificationRepository).deleteById(1L);

        assertDoesNotThrow(() -> notificationService.deleteNotification(1L));
        
        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetAllNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> mockPage = new PageImpl<>(List.of(savedNotification));
        
        when(notificationRepository.findAll(pageable)).thenReturn(mockPage);

        Page<NotificationResponse> result = notificationService.getAllNotifications(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Welcome!", result.getContent().get(0).getTitle());
        
        verify(notificationRepository, times(1)).findAll(pageable);
    }
}
