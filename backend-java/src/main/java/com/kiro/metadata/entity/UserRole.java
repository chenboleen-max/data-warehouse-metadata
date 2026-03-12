package com.kiro.metadata.entity;

/**
 * User role enumeration
 * Defines the three roles in the system with different permission levels
 */
public enum UserRole {
    /**
     * 访客角色 - read-only access
     * Can only query metadata
     */
    GUEST,
    
    /**
     * 开发者角色 - read and update access
     * Can query and edit metadata annotations
     */
    DEVELOPER,
    
    /**
     * Admin role - full access
     * Can perform all operations including delete and catalog management
     */
    ADMIN
}
