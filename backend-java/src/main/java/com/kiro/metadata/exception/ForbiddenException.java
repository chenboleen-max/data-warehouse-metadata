package com.kiro.metadata.exception;

import org.springframework.http.HttpStatus;

/**
 * 权限不足异常
 */
public class ForbiddenException extends BusinessException {
    
    public ForbiddenException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }
}
