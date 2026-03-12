package com.kiro.metadata.entity;

/**
 * Operation type enumeration
 * Defines the types of operations for change history tracking
 */
public enum OperationType {
    /**
     * Create operation
     */
    CREATE,
    
    /**
     * Update operation
     */
    UPDATE,
    
    /**
     * Delete operation
     */
    DELETE
}
