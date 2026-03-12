package com.kiro.metadata.exception;

import org.springframework.http.HttpStatus;

/**
 * Resource not found exception
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
