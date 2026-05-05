package com.airesume.ai.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global exception handler for the AI Service.
 * Converts runtime failures from the upstream AI provider or internal logic into
 * structured JSON responses that the frontend can display cleanly.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Pattern RETRY_DELAY_PATTERN = Pattern.compile("retryDelay\"\\s*:\\s*\"(\\d+)s\"");

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("AI Service error: {}", ex.getMessage());

        String message = ex.getMessage();
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);

        if (message != null && message.contains("Premium feature locked")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "error", "Premium Required",
                "message", message,
                "timestamp", LocalDateTime.now().toString()
            ));
        }

        if (normalized.contains("quota exceeded")
            || normalized.contains("resource_exhausted")
            || normalized.contains("retrydelay")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "status", 429,
                "error", "Quota Exceeded",
                "message", buildQuotaMessage(message),
                "timestamp", LocalDateTime.now().toString()
            ));
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
            "status", 503,
            "error", "AI Service Unavailable",
            "message", message != null && !message.isBlank()
                ? message
                : "AI service temporarily unavailable. Please try again in a few minutes.",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected AI Service error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "status", 500,
            "error", "Internal Server Error",
            "message", "An unexpected error occurred. Please try again.",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    private String buildQuotaMessage(String rawMessage) {
        String retryMessage = extractRetryMessage(rawMessage);
        if (retryMessage != null) {
            return retryMessage;
        }

        return "AI provider quota is temporarily exhausted. Please wait a bit and try again.";
    }

    private String extractRetryMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return null;
        }

        Matcher matcher = RETRY_DELAY_PATTERN.matcher(rawMessage);
        if (!matcher.find()) {
            return null;
        }

        return "AI provider quota is temporarily exhausted. Please retry in about "
            + matcher.group(1)
            + " seconds.";
    }
}
