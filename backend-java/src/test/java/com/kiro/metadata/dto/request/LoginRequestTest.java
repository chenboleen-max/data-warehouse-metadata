package com.kiro.metadata.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LoginRequest DTO validation
 * 
 * Validates: Requirements 6.2 (User Authentication)
 */
@DisplayName("LoginRequest Validation Tests")
class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid login request should pass validation")
    void testValidLoginRequest() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Username cannot be blank")
    void testUsernameCannotBeBlank() {
        // Given
        LoginRequest request = new LoginRequest("", "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then - Both @NotBlank and @Size validations trigger for empty string
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> 
            v.getMessage().contains("Username cannot be blank") ||
            v.getMessage().contains("Username must be between 3 and 50 characters")
        );
    }

    @Test
    @DisplayName("Username cannot be null")
    void testUsernameCannotBeNull() {
        // Given
        LoginRequest request = new LoginRequest(null, "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Username cannot be blank");
    }

    @Test
    @DisplayName("Username must be at least 3 characters")
    void testUsernameTooShort() {
        // Given
        LoginRequest request = new LoginRequest("ab", "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Username must not exceed 50 characters")
    void testUsernameTooLong() {
        // Given
        String longUsername = "a".repeat(51);
        LoginRequest request = new LoginRequest(longUsername, "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Password cannot be blank")
    void testPasswordCannotBeBlank() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then - Both @NotBlank and @Size validations trigger for empty string
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> 
            v.getMessage().contains("Password cannot be blank") ||
            v.getMessage().contains("Password must be between 6 and 100 characters")
        );
    }

    @Test
    @DisplayName("Password cannot be null")
    void testPasswordCannotBeNull() {
        // Given
        LoginRequest request = new LoginRequest("testuser", null);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Password cannot be blank");
    }

    @Test
    @DisplayName("Password must be at least 6 characters")
    void testPasswordTooShort() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "12345");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Password must be between 6 and 100 characters");
    }

    @Test
    @DisplayName("Password must not exceed 100 characters")
    void testPasswordTooLong() {
        // Given
        String longPassword = "a".repeat(101);
        LoginRequest request = new LoginRequest("testuser", longPassword);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Password must be between 6 and 100 characters");
    }

    @Test
    @DisplayName("Username at minimum length (3 chars) should be valid")
    void testUsernameMinimumLength() {
        // Given
        LoginRequest request = new LoginRequest("abc", "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Username at maximum length (50 chars) should be valid")
    void testUsernameMaximumLength() {
        // Given
        String maxUsername = "a".repeat(50);
        LoginRequest request = new LoginRequest(maxUsername, "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Password at minimum length (6 chars) should be valid")
    void testPasswordMinimumLength() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "123456");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Password at maximum length (100 chars) should be valid")
    void testPasswordMaximumLength() {
        // Given
        String maxPassword = "a".repeat(100);
        LoginRequest request = new LoginRequest("testuser", maxPassword);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}
