package com.kiro.metadata.entity;

/**
 * Table type enumeration
 * Defines the types of tables in the data warehouse
 */
public enum TableType {
    /**
     * 普通表
     */
    TABLE,
    
    /**
     * View
     */
    VIEW,
    
    /**
     * 外部表
     */
    EXTERNAL
}
