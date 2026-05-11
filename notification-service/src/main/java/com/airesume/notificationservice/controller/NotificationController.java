package com.airesume.notificationservice.controller;

import com.airesume.notificationservice.dto.BulkNotificationRequest;
import com.airesume.notificationservice.dto.NotificationRequest;
import com.airesume.notificationservice.dto.NotificationResponse;
import com.airesume.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Endpoints for sending and managing notifications and emails")
public class NotificationController {

    private final NotificationService notificationService;

    // ── Per-user endpoints ────────────────────────────────────────────────

    @GetMapping("/recipient/{userId}")
    @Operation(summary = "Get all notifications for a recipient (paginated)")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId, pageable));
    }

    @GetMapping("/recipient/{userId}/unread-count")
    @Operation(summary = "Get count of unread notifications for a user")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{notificationId}/mark-read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/recipient/{userId}/mark-all-read")
    @Operation(summary = "Mark all notifications for a user as read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    // ── Admin / Broadcast endpoints ───────────────────────────────────────

    /**
     * Admin broadcast: send a notification to all users, or filtered by tier (FREE/PREMIUM/ALL).
     * POST /send-bulk
     */
    @PostMapping("/send-bulk")
    @Operation(summary = "Send a bulk notification to users filtered by tier (Admin only)")
    public ResponseEntity<Void> sendBulk(
            @Valid @RequestBody BulkNotificationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        notificationService.sendBulkNotification(request, authorizationHeader);
        return ResponseEntity.accepted().build();
    }

    /**
     * Send a single targeted notification (internal / admin use).
     * POST /send
     */
    @PostMapping("/send")
    @Operation(summary = "Send a single targeted notification (Internal/Admin only)")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    /**
     * Admin: get all notifications (paginated).
     * GET /all
     */
    @GetMapping("/all")
    @Operation(summary = "Get all system-wide notifications (Admin only)")
    public ResponseEntity<Page<NotificationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }
}
