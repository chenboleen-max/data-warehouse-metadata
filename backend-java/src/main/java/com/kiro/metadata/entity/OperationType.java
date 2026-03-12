package com.kiro.metadata.entity;

/**
 * Operation type enumeration
 * Defines the types of operations for change history tracking
 */
public enum OperationType {
    /**
     * 创建操作
     */
    CREATE,
    
    /**
     * 更新操作
     */
    UPDATE,
    
    /**
     * 删除操作
     */
    DELETE
}
