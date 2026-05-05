package com.airesume.ai.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class CurrentUserService {

    public String requireUserIdAsString() {
        Object value = getDetail("userId");
        if (value instanceof Number number) {
            return String.valueOf(number.longValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user id is missing from the token.");
    }

    public boolean isPremium() {
        Object plan = getDetail("subscriptionPlan");
        return plan != null && "PREMIUM".equalsIgnoreCase(String.valueOf(plan));
    }

    private Object getDetail(String key) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof Map<?, ?> details)) {
            return null;
        }
        return details.get(key);
    }
}
