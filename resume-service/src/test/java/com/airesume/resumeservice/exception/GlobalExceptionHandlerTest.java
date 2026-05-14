package com.airesume.resumeservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Resume Service Global Exception Handler.
 * Verifies that structured error responses are correctly generated for 
 * standard Spring ResponseStatusExceptions.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * Verifies that ResponseStatusException is correctly mapped to a structured JSON response.
     */
    @Test
    void handleResponseStatus_ShouldReturnStructuredResponse() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Resource not found", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    /**
     * Ensures that a fallback message is provided when the exception reason is null.
     */
    @Test
    void handleResponseStatus_ShouldFallbackToMessage_WhenReasonIsNull() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("500 INTERNAL_SERVER_ERROR", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }
}
