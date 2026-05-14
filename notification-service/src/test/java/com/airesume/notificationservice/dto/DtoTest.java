package com.airesume.notificationservice.dto;

import com.airesume.notificationservice.model.NotificationTier;
import com.airesume.notificationservice.model.NotificationType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    /**
     * Verifies the basic notification request DTO fields.
     */
    @Test
    void testNotificationRequest() {
        NotificationRequest req = new NotificationRequest();
        req.setRecipientId(1L);
        req.setTitle("t");
        req.setMessage("m");
        req.setType(NotificationType.INFO);

        assertEquals(1L, req.getRecipientId());
        assertEquals("t", req.getTitle());
        assertEquals("m", req.getMessage());
        assertEquals(NotificationType.INFO, req.getType());
    }

    /**
     * Verifies the bulk notification request DTO fields and tier mapping.
     */
    @Test
    void testBulkNotificationRequest() {
        BulkNotificationRequest req = new BulkNotificationRequest();
        req.setTitle("t");
        req.setMessage("m");
        req.setType(NotificationType.SYSTEM);
        req.setTier(NotificationTier.PREMIUM);

        assertEquals("t", req.getTitle());
        assertEquals("m", req.getMessage());
        assertEquals(NotificationType.SYSTEM, req.getType());
        assertEquals(NotificationTier.PREMIUM, req.getTier());
    }

    /**
     * Verifies the notification response DTO fields and builder.
     */
    @Test
    void testNotificationResponse() {
        NotificationResponse res = NotificationResponse.builder()
                .id(1L)
                .title("t")
                .message("m")
                .type(NotificationType.INFO)
                .isRead(true)
                .build();
        
        assertEquals(1L, res.getId());
        assertEquals("t", res.getTitle());
        assertEquals("m", res.getMessage());
        assertEquals(NotificationType.INFO, res.getType());
        assertTrue(res.isRead());
        assertNotNull(res.toString());
    }

    /**
     * Verifies the email delivery request payload fields.
     */
    @Test
    void testEmailRequest() {
        EmailRequest req = new EmailRequest();
        req.setTo("to@example.com");
        req.setSubject("Subject");
        req.setBody("Body");
        req.setHtml(true);

        assertEquals("to@example.com", req.getTo());
        assertEquals("Subject", req.getSubject());
        assertEquals("Body", req.getBody());
        assertTrue(req.isHtml());
    }

    /**
     * Verifies the administrative user DTO mapping used for bulk targeting.
     */
    @Test
    void testAdminUserDto() {
        AdminUserDto dto = new AdminUserDto();
        dto.setUserId(1L);
        dto.setSubscriptionPlan("PREMIUM");
        dto.setActive(true);

        assertEquals(1L, dto.getUserId());
        assertEquals("PREMIUM", dto.getSubscriptionPlan());
        assertTrue(dto.isActive());
    }

    /**
     * Verifies the persistence model for notifications, including custom getter logic.
     */
    @Test
    void testNotificationModel() {
        com.airesume.notificationservice.model.Notification notification = 
                com.airesume.notificationservice.model.Notification.builder()
                        .id(1L)
                        .recipientId(1L)
                        .title("Test Title")
                        .message("Test Message")
                        .type(NotificationType.INFO)
                        .tier(NotificationTier.ALL)
                        .isRead(false)
                        .build();

        assertEquals(1L, notification.getId());
        assertEquals(1L, notification.getRecipientId());
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Message", notification.getMessage());
        assertEquals(NotificationType.INFO, notification.getType());
        assertEquals(NotificationTier.ALL, notification.getTier());
        assertFalse(notification.isRead());
        assertEquals("Test Title", notification.getTitleManual());
    }

    /**
     * Verifies that the notification entity's creation timestamp is correctly initialized.
     */
    @Test
    void testNotificationOnCreate() {
        com.airesume.notificationservice.model.Notification notification = 
                com.airesume.notificationservice.model.Notification.builder().build();
        assertNull(notification.getCreatedAt());

        org.springframework.test.util.ReflectionTestUtils.invokeMethod(notification, "onCreate");

        assertNotNull(notification.getCreatedAt());
    }
}
