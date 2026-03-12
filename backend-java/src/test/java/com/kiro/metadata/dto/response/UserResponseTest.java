package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserResponse DTO
 * 
 * Validates: Requirements 6.2 (User Authentication)
 */
@DisplayName("UserResponse Tests")
class UserResponseTest {

    @Test
    @DisplayName("Should create UserResponse with all fields")
    void testCreateUserResponseWithAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        String username = "testuser";
        String email = "test@example.com";
        UserRole role = UserRole.DEVELOPER;
        Boolean isActive = true;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        UserResponse response = new UserResponse(id, username, email, role, isActive, createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getRole()).isEqualTo(role);
        assertThat(response.getIsActive()).isEqualTo(isActive);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should create empty UserResponse with no-args constructor")
    void testNoArgsConstructor() {
        // When
        UserResponse response = new UserResponse();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getRole()).isNull();
        assertThat(response.getIsActive()).isNull();
        assertThat(response.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should allow setting fields via setters")
    void testSetters() {
        // Given
        UserResponse response = new UserResponse();
        UUID id = UUID.randomUUID();
        String username = "newuser";
        String email = "new@example.com";
        UserRole role = UserRole.ADMIN;
        Boolean isActive = false;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        response.setId(id);
        response.setUsername(username);
        response.setEmail(email);
        response.setRole(role);
        response.setIsActive(isActive);
        response.setCreatedAt(createdAt);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getRole()).isEqualTo(role);
        assertThat(response.getIsActive()).isEqualTo(isActive);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should support all UserRole values")
    void testAllUserRoles() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        // When & Then - ADMIN
        UserResponse adminResponse = new UserResponse(id, "admin", "admin@example.com", 
            UserRole.ADMIN, true, createdAt);
        assertThat(adminResponse.getRole()).isEqualTo(UserRole.ADMIN);

        // When & Then - DEVELOPER
        UserResponse devResponse = new UserResponse(id, "dev", "dev@example.com", 
            UserRole.DEVELOPER, true, createdAt);
        assertThat(devResponse.getRole()).isEqualTo(UserRole.DEVELOPER);

        // When & Then - GUEST
        UserResponse guestResponse = new UserResponse(id, "guest", "guest@example.com", 
            UserRole.GUEST, true, createdAt);
        assertThat(guestResponse.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    @DisplayName("Should support active and inactive users")
    void testActiveStatus() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        // When - Active user
        UserResponse activeUser = new UserResponse(id, "active", "active@example.com", 
            UserRole.DEVELOPER, true, createdAt);

        // Then
        assertThat(activeUser.getIsActive()).isTrue();

        // When - Inactive user
        UserResponse inactiveUser = new UserResponse(id, "inactive", "inactive@example.com", 
            UserRole.DEVELOPER, false, createdAt);

        // Then
        assertThat(inactiveUser.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void testEqualsAndHashCode() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        UserResponse response1 = new UserResponse(id, "user", "user@example.com", 
            UserRole.DEVELOPER, true, createdAt);
        UserResponse response2 = new UserResponse(id, "user", "user@example.com", 
            UserRole.DEVELOPER, true, createdAt);
        UserResponse response3 = new UserResponse(UUID.randomUUID(), "other", "other@example.com", 
            UserRole.GUEST, false, createdAt);

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).hasSameHashCodeAs(response2);
        assertThat(response1).isNotEqualTo(response3);
    }

    @Test
    @DisplayName("Should support toString")
    void testToString() {
        // Given
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse(id, "testuser", "test@example.com", 
            UserRole.DEVELOPER, true, LocalDateTime.now());

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("id");
        assertThat(toString).contains("username");
        assertThat(toString).contains("email");
        assertThat(toString).contains("role");
        assertThat(toString).contains("isActive");
        assertThat(toString).contains("createdAt");
    }

    @Test
    @DisplayName("Should not expose password information")
    void testNoPasswordField() {
        // Given
        UserResponse response = new UserResponse();

        // When
        String toString = response.toString();

        // Then - Verify no password-related fields exist
        assertThat(toString).doesNotContain("password");
        assertThat(toString).doesNotContain("passwordHash");
    }
}
