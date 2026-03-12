package com.kiro.metadata.exception;

/**
 * 未授权异常
 * 当用户未登录或认证失败时抛出
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
