package com.airesume.templateservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resume template cannot be found.
 * Annotated with 404 NOT_FOUND for automatic Spring MVC handling.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TemplateNotFoundException extends RuntimeException {
    
    public TemplateNotFoundException(String message) {
        super(message);
    }
}
