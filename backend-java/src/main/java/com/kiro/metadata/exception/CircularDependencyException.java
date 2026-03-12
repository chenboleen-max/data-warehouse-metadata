package com.kiro.metadata.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 循环依赖异常
 */
public class CircularDependencyException extends BusinessException {
    
    public CircularDependencyException(List<UUID> cyclePath) {
        super("CIRCULAR_DEPENDENCY", 
              "检测到循环依赖", 
              HttpStatus.BAD_REQUEST,
              Map.of("cycle_path", cyclePath));
    }
}
