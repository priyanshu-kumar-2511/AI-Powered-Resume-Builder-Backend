package com.airesume.authservice.controller;

import com.airesume.authservice.dto.UserProfileResponse;
import com.airesume.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "admin-controller", description = "Administrative endpoints for user management and platform oversight")
public class AdminController {

    private final AuthService authService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves a comprehensive list of all users and their subscription statuses")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PutMapping("/users/{username}/status")
    @Operation(summary = "Update user status", description = "Suspend or reactivate a user account by its username")
    public ResponseEntity<Map<String, String>> updateUserStatus(@PathVariable String username, @RequestParam boolean active) {
        String result = authService.updateUserStatus(username, active);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/users/{username}/role")
    @Operation(summary = "Update user role", description = "Promote or demote a user to a specific role (e.g., ROLE_ADMIN, ROLE_USER)")
    public ResponseEntity<Map<String, String>> updateUserRole(@PathVariable String username, @RequestParam String roleName) {
        String result = authService.updateUserRole(username, roleName);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Filter users by role", description = "Retrieves a list of users filtered by their assigned role (e.g., ROLE_USER, ROLE_ADMIN)")
    public ResponseEntity<List<UserProfileResponse>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(authService.getUsersByRole(role));
    }

    @GetMapping("/users/plan/{plan}")
    @Operation(summary = "Filter users by plan", description = "Retrieves a list of users filtered by their subscription tier (FREE/PREMIUM)")
    public ResponseEntity<List<UserProfileResponse>> getUsersByPlan(@PathVariable com.airesume.authservice.model.PlanType plan) {
        return ResponseEntity.ok(authService.getUsersByPlan(plan));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user permanently", description = "Permanently removes a user record and its associated data from the platform")
    public ResponseEntity<Map<String, String>> deleteUserPermanently(@PathVariable Long userId) {
        String result = authService.deleteUserPermanently(userId);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
