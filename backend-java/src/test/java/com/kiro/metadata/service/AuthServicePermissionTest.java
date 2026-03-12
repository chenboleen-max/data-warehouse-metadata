package com.kiro.metadata.service;

import com.kiro.metadata.entity.User;
import com.kiro.metadata.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuthService permission verification methods
 * 
 * Tests role-based access control (RBAC) implementation:
 * - GUEST: read-only access
 * - DEVELOPER: read and update access
 * - ADMIN: full access
 * 
 * Validates: Requirements 6.1, 6.3, 6.4, 6.5
 */
@DisplayName("AuthService Permission Tests")
class AuthServicePermissionTest {

    private AuthService authService;

    private User guestUser;
    private User developerUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Create AuthService instance without mocking dependencies
        // checkPermission method doesn't use any dependencies
        authService = new AuthService(null, null, null, null);
        
        // Create test users with different roles
        guestUser = createUser("guest", UserRole.GUEST);
        developerUser = createUser("developer", UserRole.DEVELOPER);
        adminUser = createUser("admin", UserRole.ADMIN);
    }

    private User createUser(String username, UserRole role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("$2a$10$hashedPassword");
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    // ========== GUEST Role Tests ==========

    @Test
    @DisplayName("GUEST can perform read action")
    void testGuestCanRead() {
        boolean hasPermission = authService.checkPermission(guestUser, "table", "read");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("GUEST cannot perform update action")
    void testGuestCannotUpdate() {
        boolean hasPermission = authService.checkPermission(guestUser, "table", "update");
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("GUEST cannot perform delete action")
    void testGuestCannotDelete() {
        boolean hasPermission = authService.checkPermission(guestUser, "table", "delete");
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("GUEST cannot perform create_catalog action")
    void testGuestCannotCreateCatalog() {
        boolean hasPermission = authService.checkPermission(guestUser, "catalog", "create_catalog");
        assertThat(hasPermission).isFalse();
    }

    // ========== DEVELOPER Role Tests ==========

    @Test
    @DisplayName("DEVELOPER can perform read action")
    void testDeveloperCanRead() {
        boolean hasPermission = authService.checkPermission(developerUser, "table", "read");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("DEVELOPER can perform update action")
    void testDeveloperCanUpdate() {
        boolean hasPermission = authService.checkPermission(developerUser, "table", "update");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("DEVELOPER cannot perform delete action")
    void testDeveloperCannotDelete() {
        boolean hasPermission = authService.checkPermission(developerUser, "table", "delete");
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("DEVELOPER cannot perform create_catalog action")
    void testDeveloperCannotCreateCatalog() {
        boolean hasPermission = authService.checkPermission(developerUser, "catalog", "create_catalog");
        assertThat(hasPermission).isFalse();
    }

    // ========== ADMIN Role Tests ==========

    @Test
    @DisplayName("ADMIN can perform read action")
    void testAdminCanRead() {
        boolean hasPermission = authService.checkPermission(adminUser, "table", "read");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("ADMIN can perform update action")
    void testAdminCanUpdate() {
        boolean hasPermission = authService.checkPermission(adminUser, "table", "update");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("ADMIN can perform delete action")
    void testAdminCanDelete() {
        boolean hasPermission = authService.checkPermission(adminUser, "table", "delete");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("ADMIN can perform create_catalog action")
    void testAdminCanCreateCatalog() {
        boolean hasPermission = authService.checkPermission(adminUser, "catalog", "create_catalog");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("ADMIN can perform any action")
    void testAdminCanPerformAnyAction() {
        assertThat(authService.checkPermission(adminUser, "any", "custom_action")).isTrue();
        assertThat(authService.checkPermission(adminUser, "any", "another_action")).isTrue();
    }

    // ========== Edge Cases and Null Handling ==========

    @Test
    @DisplayName("checkPermission returns false when user is null")
    void testCheckPermissionWithNullUser() {
        boolean hasPermission = authService.checkPermission(null, "table", "read");
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("checkPermission returns false when action is null")
    void testCheckPermissionWithNullAction() {
        boolean hasPermission = authService.checkPermission(guestUser, "table", null);
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("checkPermission is case-insensitive for actions")
    void testCheckPermissionCaseInsensitive() {
        assertThat(authService.checkPermission(guestUser, "table", "READ")).isTrue();
        assertThat(authService.checkPermission(guestUser, "table", "Read")).isTrue();
        assertThat(authService.checkPermission(guestUser, "table", "read")).isTrue();
        
        assertThat(authService.checkPermission(developerUser, "table", "UPDATE")).isTrue();
        assertThat(authService.checkPermission(developerUser, "table", "Update")).isTrue();
        assertThat(authService.checkPermission(developerUser, "table", "update")).isTrue();
    }

    // ========== Permission Boundary Tests ==========

    @Test
    @DisplayName("Test all role boundaries for read action")
    void testReadActionForAllRoles() {
        assertThat(authService.checkPermission(guestUser, "table", "read"))
            .as("GUEST should be able to read")
            .isTrue();
        
        assertThat(authService.checkPermission(developerUser, "table", "read"))
            .as("DEVELOPER should be able to read")
            .isTrue();
        
        assertThat(authService.checkPermission(adminUser, "table", "read"))
            .as("ADMIN should be able to read")
            .isTrue();
    }

    @Test
    @DisplayName("Test all role boundaries for update action")
    void testUpdateActionForAllRoles() {
        assertThat(authService.checkPermission(guestUser, "table", "update"))
            .as("GUEST should NOT be able to update")
            .isFalse();
        
        assertThat(authService.checkPermission(developerUser, "table", "update"))
            .as("DEVELOPER should be able to update")
            .isTrue();
        
        assertThat(authService.checkPermission(adminUser, "table", "update"))
            .as("ADMIN should be able to update")
            .isTrue();
    }

    @Test
    @DisplayName("Test all role boundaries for delete action")
    void testDeleteActionForAllRoles() {
        assertThat(authService.checkPermission(guestUser, "table", "delete"))
            .as("GUEST should NOT be able to delete")
            .isFalse();
        
        assertThat(authService.checkPermission(developerUser, "table", "delete"))
            .as("DEVELOPER should NOT be able to delete")
            .isFalse();
        
        assertThat(authService.checkPermission(adminUser, "table", "delete"))
            .as("ADMIN should be able to delete")
            .isTrue();
    }

    @Test
    @DisplayName("Test all role boundaries for create_catalog action")
    void testCreateCatalogActionForAllRoles() {
        assertThat(authService.checkPermission(guestUser, "catalog", "create_catalog"))
            .as("GUEST should NOT be able to create catalog")
            .isFalse();
        
        assertThat(authService.checkPermission(developerUser, "catalog", "create_catalog"))
            .as("DEVELOPER should NOT be able to create catalog")
            .isFalse();
        
        assertThat(authService.checkPermission(adminUser, "catalog", "create_catalog"))
            .as("ADMIN should be able to create catalog")
            .isTrue();
    }
}
