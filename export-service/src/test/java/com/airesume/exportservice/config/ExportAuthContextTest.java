package com.airesume.exportservice.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Export Authorization Context.
 * Verifies ThreadLocal behavior for propagation of security tokens 
 * during background export processing.
 */
class ExportAuthContextTest {

    /**
     * Verifies that the ThreadLocal storage correctly preserves and clears authorization headers.
     */
    @Test
    void testExportAuthContextThreadLocal() {
        ExportAuthContext.setAuthorization("Bearer myToken");
        assertEquals("Bearer myToken", ExportAuthContext.getAuthorization());

        ExportAuthContext.clear();
        assertNull(ExportAuthContext.getAuthorization());
    }

    /**
     * Verifies the utility class pattern (private constructor coverage).
     */
    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<ExportAuthContext> constructor = ExportAuthContext.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        
        ExportAuthContext instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
