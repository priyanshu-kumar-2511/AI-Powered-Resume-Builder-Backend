package com.airesume.notificationservice.controller;

import com.airesume.notificationservice.dto.BulkNotificationRequest;
import com.airesume.notificationservice.dto.NotificationRequest;
import com.airesume.notificationservice.dto.NotificationResponse;
import com.airesume.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ── Per-user endpoints ────────────────────────────────────────────────

    @GetMapping("/recipient/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId, pageable));
    }

    @GetMapping("/recipient/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/recipient/{userId}/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
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
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    /**
     * Admin: get all notifications (paginated).
     * GET /all
     */
    @GetMapping("/all")
    public ResponseEntity<Page<NotificationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }
}
