package com.airesume.resumeservice.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Helper Service to extract current user details from the Security Context.
 * Simplifies access to User ID, Subscription Plan, and Admin roles embedded 
 * in the JWT claims.
 */
@Service
public class CurrentUserService {

    /**
     * Extracts the User ID from the JWT details.
     * Throws 401 UNAUTHORIZED if the ID is missing.
     */
    public Long requireUserId() {
        Object value = getDetail("userId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user id is missing from the token.");
    }

    /**
     * Checks if the current user has a PREMIUM subscription plan.
     */
    public boolean isPremium() {
        Object plan = getDetail("subscriptionPlan");
        return plan != null && "PREMIUM".equalsIgnoreCase(String.valueOf(plan));
    }

    /**
     * Checks if the current user has ROLE_ADMIN.
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities() != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access is required for this operation.");
        }
    }

    private Object getDetail(String key) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof Map<?, ?> details)) {
            return null;
        }
        return details.get(key);
    }
}
