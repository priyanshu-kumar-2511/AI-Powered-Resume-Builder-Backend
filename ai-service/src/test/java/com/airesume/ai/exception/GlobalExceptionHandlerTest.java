package com.airesume.ai.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleRuntimeException_NullMessage() {
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(new RuntimeException((String)null));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("temporarily unavailable"));
    }

    @Test
    void testHandleRuntimeException_ResourceExhausted() {
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(new RuntimeException("Resource_Exhausted"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("quota is temporarily exhausted"));
    }

    @Test
    void testHandleRuntimeException_RetryDelay() {
        // Test with retryDelay in message
        String msg = "Something went wrong. retryDelay\": \"60s\"";
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(new RuntimeException(msg));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("retry in about 60 seconds"));
    }

    @Test
    void testHandleRuntimeException_PremiumLocked() {
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(new RuntimeException("Premium feature locked"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testHandleGenericException() {
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(new Exception("Generic"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testExtractRetryMessage_Blank() {
        // Internal method test via handleRuntimeException with specifically crafted message
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(new RuntimeException("quota exceeded but no delay here"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("quota is temporarily exhausted"));
    }

    @Test
    void testPrivateMethods_Reflection() {
        // test extractRetryMessage with null, empty, blank
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(handler, "extractRetryMessage", (Object) null);
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(handler, "extractRetryMessage", "");
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(handler, "extractRetryMessage", "   ");

        // test handleRuntimeException with blank message
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(new RuntimeException("   "));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("temporarily unavailable"));
    }
}
