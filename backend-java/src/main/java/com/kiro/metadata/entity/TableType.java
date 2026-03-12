package com.kiro.metadata.entity;

/**
 * Table type enumeration
 * Defines the types of tables in the data warehouse
 */
public enum TableType {
    /**
     * Regular table
     */
    TABLE,
    
    /**
     * View
     */
    VIEW,
    
    /**
     * External table
     */
    EXTERNAL
}
