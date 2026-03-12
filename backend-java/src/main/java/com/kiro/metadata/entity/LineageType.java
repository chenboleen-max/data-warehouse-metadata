package com.kiro.metadata.entity;

/**
 * Lineage type enumeration
 * Defines the types of lineage relationships between tables
 */
public enum LineageType {
    /**
     * Direct lineage - direct dependency between tables
     */
    DIRECT,
    
    /**
     * Indirect lineage - indirect dependency through intermediate tables
     */
    INDIRECT
}
