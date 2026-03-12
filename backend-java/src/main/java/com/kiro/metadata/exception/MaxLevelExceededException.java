package com.kiro.metadata.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 目录层级超限异常
 */
public class MaxLevelExceededException extends BusinessException {
    
    public MaxLevelExceededException(int currentLevel, int maxLevel) {
        super("MAX_LEVEL_EXCEEDED", 
              "目录层级不能超过 " + maxLevel + " 级", 
              HttpStatus.BAD_REQUEST,
              Map.of("current_level", currentLevel, "max_level", maxLevel));
    }
}
