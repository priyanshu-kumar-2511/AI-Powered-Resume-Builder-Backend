package com.airesume.sectionservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleResponseStatus_ShouldReturnStatusAndReason() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResponseStatus(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Section not found", body.get("message"));
        assertEquals(404, body.get("status"));
    }

    @Test
    void handleRuntime_ShouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Some unexpected error");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntime(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Some unexpected error", body.get("message"));
        assertEquals(500, body.get("status"));
    }

    @Test
    void handleResponseStatus_ShouldUseDefaultReason_WhenNull() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, null);
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResponseStatus(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Request failed", body.get("message"));
    }

    @Test
    void handleRuntime_ShouldUseDefaultMessage_WhenNull() {
        RuntimeException ex = new RuntimeException((String) null);
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntime(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("An unexpected error occurred.", body.get("message"));
    }
}
