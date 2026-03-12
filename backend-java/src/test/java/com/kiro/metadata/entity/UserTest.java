package com.kiro.metadata.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 * Tests validation, getters/setters, and business logic
 */
@DisplayName("User Entity Tests")
class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid user with all required fields")
    void testCreateValidUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hashedpassword");
        user.setRole(UserRole.DEVELOPER);
        user.setIsActive(true);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertTrue(violations.isEmpty(), "Valid user should have no validation errors");
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(UserRole.DEVELOPER, user.getRole());
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("Should fail validation when username is blank")
    void testUsernameCannotBeBlank() {
        // Given
        User user = new User();
        user.setUsername("");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hashedpassword");
        user.setRole(UserRole.GUEST);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("Should fail validation when username is too short")
    void testUsernameTooShort() {
        // Given
        User user = new User();
        user.setUsername("ab");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hashedpassword");
        user.setRole(UserRole.GUEST);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("Should fail validation when email is invalid")
    void testInvalidEmail() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("invalid-email");
        user.setPasswordHash("$2a$10$hashedpassword");
        user.setRole(UserRole.GUEST);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Should support all three user roles")
    void testUserRoles() {
        // Given & When & Then
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, admin.getRole());

        User developer = new User();
        developer.setRole(UserRole.DEVELOPER);
        assertEquals(UserRole.DEVELOPER, developer.getRole());

        User guest = new User();
        guest.setRole(UserRole.GUEST);
        assertEquals(UserRole.GUEST, guest.getRole());
    }

    @Test
    @DisplayName("Should default isActive to true")
    void testDefaultIsActive() {
        // Given
        User user = new User();

        // When & Then
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("Should set and get lastLoginAt timestamp")
    void testLastLoginAt() {
        // Given
        User user = new User();
        LocalDateTime loginTime = LocalDateTime.now();

        // When
        user.setLastLoginAt(loginTime);

        // Then
        assertEquals(loginTime, user.getLastLoginAt());
    }

    @Test
    @DisplayName("Should inherit audit fields from BaseEntity")
    void testInheritedAuditFields() {
        // Given
        User user = new User();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        user.setId(id);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setIsDeleted(false);

        // Then
        assertEquals(id, user.getId());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertFalse(user.getIsDeleted());
    }

    @Test
    @DisplayName("Should create user using all-args constructor")
    void testAllArgsConstructor() {
        // Given & When
        User user = new User(
            "testuser",
            "test@example.com",
            "$2a$10$hashedpassword",
            UserRole.DEVELOPER,
            true,
            LocalDateTime.now()
        );

        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(UserRole.DEVELOPER, user.getRole());
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("Should create user using no-args constructor")
    void testNoArgsConstructor() {
        // Given & When
        User user = new User();

        // Then
        assertNotNull(user);
        assertTrue(user.getIsActive());
    }
}
