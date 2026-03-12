package com.kiro.metadata.exception;

import org.springframework.http.HttpStatus;

/**
 * 资源重复异常
 */
public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message, HttpStatus.CONFLICT);
    }
}
