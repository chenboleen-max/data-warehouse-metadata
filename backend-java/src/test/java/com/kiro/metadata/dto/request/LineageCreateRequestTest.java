package com.kiro.metadata.dto.request;

import com.kiro.metadata.entity.LineageType;
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
 * Unit tests for LineageCreateRequest DTO validation
 * 
 * Validates: Requirements 3.1, 3.2 (Lineage Relationship Management)
 */
@DisplayName("LineageCreateRequest Validation Tests")
class LineageCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid lineage create request should pass validation")
    void testValidLineageCreateRequest() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LineageType.DIRECT,
            "INSERT INTO target SELECT * FROM source"
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Source table ID cannot be null")
    void testSourceTableIdCannotBeNull() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            null,
            UUID.randomUUID(),
            LineageType.DIRECT,
            null
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Source table ID cannot be null");
    }

    @Test
    @DisplayName("Target table ID cannot be null")
    void testTargetTableIdCannotBeNull() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            UUID.randomUUID(),
            null,
            LineageType.DIRECT,
            null
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Target table ID cannot be null");
    }

    @Test
    @DisplayName("Lineage type cannot be null")
    void testLineageTypeCannotBeNull() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Lineage type cannot be null");
    }

    @Test
    @DisplayName("Transformation logic must not exceed 10000 characters")
    void testTransformationLogicTooLong() {
        // Given
        String longLogic = "a".repeat(10001);
        LineageCreateRequest request = new LineageCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LineageType.DIRECT,
            longLogic
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Transformation logic must not exceed 10000 characters");
    }

    @Test
    @DisplayName("Transformation logic can be null")
    void testTransformationLogicCanBeNull() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LineageType.INDIRECT,
            null
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should support DIRECT lineage type")
    void testDirectLineageType() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LineageType.DIRECT,
            "Direct transformation"
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getLineageType()).isEqualTo(LineageType.DIRECT);
    }

    @Test
    @DisplayName("Should support INDIRECT lineage type")
    void testIndirectLineageType() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LineageType.INDIRECT,
            "Indirect transformation through temp tables"
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getLineageType()).isEqualTo(LineageType.INDIRECT);
    }

    @Test
    @DisplayName("Multiple validation errors should be reported")
    void testMultipleValidationErrors() {
        // Given
        LineageCreateRequest request = new LineageCreateRequest(
            null,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<LineageCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
    }
}
