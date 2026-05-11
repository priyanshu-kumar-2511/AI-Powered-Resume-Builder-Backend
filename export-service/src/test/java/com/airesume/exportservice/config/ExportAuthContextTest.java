package com.airesume.exportservice.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ExportAuthContextTest {

    @Test
    void testExportAuthContextThreadLocal() {
        ExportAuthContext.setAuthorization("Bearer myToken");
        assertEquals("Bearer myToken", ExportAuthContext.getAuthorization());

        ExportAuthContext.clear();
        assertNull(ExportAuthContext.getAuthorization());
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<ExportAuthContext> constructor = ExportAuthContext.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        
        ExportAuthContext instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
