package com.kiro.metadata.entity;

/**
 * Lineage type enumeration
 * Defines the types of lineage relationships between tables
 */
public enum LineageType {
    /**
     * 直接血缘 - direct dependency between tables
     */
    DIRECT,
    
    /**
     * In直接血缘 - indirect dependency through intermediate tables
     */
    INDIRECT
}
