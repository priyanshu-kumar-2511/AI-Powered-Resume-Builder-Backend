package com.airesume.notificationservice.controller;

import com.airesume.notificationservice.dto.BulkNotificationRequest;
import com.airesume.notificationservice.dto.NotificationRequest;
import com.airesume.notificationservice.dto.NotificationResponse;
import com.airesume.notificationservice.service.JwtService;
import com.airesume.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the Notification Controller.
 * Verifies notification delivery, status management (read/unread),
 * and bulk notification broadcasting.
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that a user can retrieve their paginated notification list.
     */
    @Test
    @WithMockUser
    void testGetNotifications() throws Exception {
        when(notificationService.getNotificationsForUser(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/recipient/1"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies the successful submission of a single notification request.
     */
    @Test
    @WithMockUser
    void testSendNotification() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(1L);
        request.setTitle("Hi");
        request.setMessage("Test body");

        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().build());

        mockMvc.perform(post("/send")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies retrieval of unread notification count for a specific user.
     */
    @Test
    @WithMockUser
    void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/recipient/1/unread-count"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that a specific notification can be marked as read.
     */
    @Test
    @WithMockUser
    void testMarkAsRead() throws Exception {
        mockMvc.perform(put("/1/mark-read")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifies that all notifications for a user can be marked as read in bulk.
     */
    @Test
    @WithMockUser
    void testMarkAllAsRead() throws Exception {
        mockMvc.perform(put("/recipient/1/mark-all-read")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifies that a user can delete a specific notification.
     */
    @Test
    @WithMockUser
    void testDeleteNotification() throws Exception {
        mockMvc.perform(delete("/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Tests the bulk notification endpoint used for broadcasting messages to user tiers.
     */
    @Test
    @WithMockUser
    void testSendBulk() throws Exception {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setTitle("Bulk Hi");
        request.setMessage("Bulk Body");
        request.setTier(com.airesume.notificationservice.model.NotificationTier.ALL);

        mockMvc.perform(post("/send-bulk")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    /**
     * Verifies administrative retrieval of all notifications across the system.
     */
    @Test
    @WithMockUser
    void testGetAll() throws Exception {
        when(notificationService.getAllNotifications(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/all"))
                .andExpect(status().isOk());
    }
}
