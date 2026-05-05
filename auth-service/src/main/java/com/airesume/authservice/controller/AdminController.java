package com.airesume.authservice.controller;

import com.airesume.authservice.dto.UserProfileResponse;
import com.airesume.authservice.service.AuthService;
import com.airesume.authservice.model.PlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only endpoints for user management.
 * Gateway path: /api/v1/admin/**
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;

    // ── User listing ─────────────────────────────────────────────────────────

    /**
     * Retrieves a list of all users in the system.
     * @return a list containing profile information of all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    // ── Suspend / Reactivate ─────────────────────────────────────────────────

    /**
     * Suspends a user by their ID. A suspension email containing the reason
     * and Code of Conduct is sent to the user.
     * @param userId the ID of the user to suspend
     * @param body a map containing the reason for suspension (optional)
     * @return a success message confirming the suspension
     */
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<Map<String,String>> suspendUser(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String,String> body) {
        String reason = (body != null && body.containsKey("reason") && !body.get("reason").isBlank())
                ? body.get("reason")
                : "Violation of ResumeAI Terms of Service and Code of Conduct.";
        return ResponseEntity.ok(Map.of("message", authService.suspendUserById(userId, reason)));
    }

    /**
     * Reactivates a previously suspended user and sends a reactivation email.
     * @param userId the ID of the user to reactivate
     * @return a success message confirming the reactivation
     */
    @PutMapping("/users/{userId}/reactivate")
    public ResponseEntity<Map<String,String>> reactivateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("message", authService.reactivateUserById(userId)));
    }

    // ── Subscription plan ────────────────────────────────────────────────────

    /**
     * Updates the subscription plan of a specific user.
     * Body format: { "plan": "PREMIUM" } or { "plan": "FREE" }
     * @param userId the ID of the user whose subscription is being updated
     * @param body a map containing the new subscription plan
     * @return a success message confirming the subscription change
     */
    @PutMapping("/users/{userId}/subscription")
    public ResponseEntity<Map<String,String>> updateSubscription(
            @PathVariable Long userId,
            @RequestBody Map<String,String> body) {
        PlanType plan = PlanType.valueOf(body.getOrDefault("plan","FREE").toUpperCase());
        return ResponseEntity.ok(Map.of("message", authService.updateSubscriptionById(userId, plan)));
    }

    // ── Role management ──────────────────────────────────────────────────────

    /**
     * Updates the access role of a specific user.
     * Body format: { "role": "ROLE_ADMIN" } or { "role": "ROLE_USER" }
     * @param userId the ID of the user whose role is being updated
     * @param body a map containing the new role
     * @return a success message confirming the role change
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String,String>> updateRole(
            @PathVariable Long userId,
            @RequestBody Map<String,String> body) {
        String role = body.getOrDefault("role","ROLE_USER");
        return ResponseEntity.ok(Map.of("message", authService.updateUserRoleById(userId, role)));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    /**
     * Permanently deletes a user from the system.
     * @param userId the ID of the user to delete
     * @return a success message confirming the deletion
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String,String>> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("message", authService.deleteUserPermanently(userId)));
    }

    // ── Audit Logs ───────────────────────────────────────────────────────────

    /**
     * Returns a list of all significant platform audit log events.
     * Includes: user suspensions, reactivations, role changes, plan changes, deletions.
     * GET /api/v1/admin/audit-logs
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<Map<String,Object>>> getAuditLogs() {
        return ResponseEntity.ok(authService.getAuditLogs());
    }
}
