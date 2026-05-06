package com.airesume.authservice.controller;

import com.airesume.authservice.dto.UpdatePlanRequest;
import com.airesume.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AuthService authService;

    @PostMapping("/update-plan")
    public ResponseEntity<Map<String, String>> updatePlan(@RequestBody UpdatePlanRequest request) {
        String message = authService.updateSubscriptionByUsername(request.getUsername(), request.getPlan());
        String token = authService.refreshToken(request.getUsername());
        return ResponseEntity.ok(Map.of(
                "message", message,
                "token", token
        ));
    }
}
