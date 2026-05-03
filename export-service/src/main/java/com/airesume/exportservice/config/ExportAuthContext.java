package com.airesume.exportservice.config;

public final class ExportAuthContext {

    private static final ThreadLocal<String> AUTHORIZATION = new ThreadLocal<>();

    private ExportAuthContext() {
    }

    public static void setAuthorization(String authorization) {
        AUTHORIZATION.set(authorization);
    }

    public static String getAuthorization() {
        return AUTHORIZATION.get();
    }

    public static void clear() {
        AUTHORIZATION.remove();
    }
}
