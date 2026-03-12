package com.kiro.metadata.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ColumnCreateRequest DTO validation
 * 
 * Validates: Requirements 2.1 (Column Metadata Management)
 */
@DisplayName("ColumnCreateRequest Validation Tests")
class ColumnCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid column create request should pass validation")
    void testValidColumnCreateRequest() {
        // Given
        ColumnCreateRequest request = new ColumnCreateRequest(
            UUID.randomUUID(),
            "user_id",
            "BIGINT",
            1,
            false,
            false,
            "User identifier"
        );

        // When
        Set<ConstraintViolation<ColumnCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Table ID cannot be null")
    void testTableIdCannotBeNull() {
        // Given
        ColumnCreateRequest request = new ColumnCreateRequest(
            null,
            "user_id",
            "BIGINT",
            1,
            true,
            false,
            null
        );

        // When
        Set<ConstraintViolation<ColumnCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Table ID cannot be null");
    }

    @Test
    @DisplayName("Column name cannot be blank")
    void testColumnNameCannotBeBlank() {
        // Given
        ColumnCreateRequest request = new ColumnCreateRequest(
            UUID.randomUUID(),
            "",
            "BIGINT",
            1,
            true,
            false,
            null
        );

        // When
        Set<ConstraintViolation<ColumnCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> 
            v.getMessage().contains("Column name cannot be blank")
        );
    }

    @Test
    @DisplayName("Data type cannot be blank")
    void testDataTypeCannotBeBlank() {
        // Given
        ColumnCreateRequest request = new ColumnCreateRequest(
            UUID.randomUUID(),
            "user_id",
            "",
            1,
            true,
            false,
            null
        );

        // When
        Set<ConstraintViolation<ColumnCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> 
            v.getMessage().contains("Data type cannot be blank")
        );
    }

    @Test
    @DisplayName("Column order cannot be null")
    void testColumnOrderCannotBeNull() {
        // Given
        ColumnCreateRequest request = new ColumnCreateRequest(
            UUID.randomUUID(),
            "user_id",
            "BIGINT",
            null,
            true,
            false,
            null
        );

        // When
        Set<ConstraintViolation<ColumnCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Column order cannot be null");
    }

    @Test
    @DisplayName("Optional fields can be null")
    void testOptionalFieldsCanBeNull() {
        // Given
        ColumnCreateRequest request = new ColumnCreateRequest(
            UUID.randomUUID(),
            "user_id",
            "BIGINT",
            1,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<ColumnCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Description must not exceed 1000 characters")
    void testDescriptionTooLong() {
        // Given
        String longDescription = "a".repeat(1001);
        ColumnCreateRequest request = new ColumnCreateRequest(
            UUID.randomUUID(),
            "user_id",
            "BIGINT",
            1,
            true,
            false,
            longDescription
        );

        // When
        Set<ConstraintViolation<ColumnCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Description must not exceed 1000 characters");
    }
}
