package com.kiro.metadata.entity;

/**
 * User role enumeration
 * Defines the three roles in the system with different permission levels
 */
public enum UserRole {
    /**
     * Guest role - read-only access
     * Can only query metadata
     */
    GUEST,
    
    /**
     * Developer role - read and update access
     * Can query and edit metadata annotations
     */
    DEVELOPER,
    
    /**
     * Admin role - full access
     * Can perform all operations including delete and catalog management
     */
    ADMIN
}
