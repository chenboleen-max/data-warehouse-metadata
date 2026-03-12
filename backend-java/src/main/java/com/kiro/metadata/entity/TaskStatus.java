package com.kiro.metadata.entity;

/**
 * Task status enumeration
 * Defines the status of export tasks
 */
public enum TaskStatus {
    /**
     * Task is pending execution
     */
    PENDING,
    
    /**
     * Task is currently running
     */
    RUNNING,
    
    /**
     * Task completed successfully
     */
    COMPLETED,
    
    /**
     * Task failed with error
     */
    FAILED
}
