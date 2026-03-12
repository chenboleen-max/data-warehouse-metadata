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
 * Unit tests for PageRequest DTO validation
 * 
 * Validates: Requirements 1.4 (Pagination Support)
 */
@DisplayName("PageRequest Validation Tests")
class PageRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid page request should pass validation")
    void testValidPageRequest() {
        // Given
        PageRequest request = new PageRequest(0, 20, "createdAt", PageRequest.SortOrder.DESC);

        // When
        Set<ConstraintViolation<PageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Default values should be valid")
    void testDefaultValues() {
        // Given
        PageRequest request = new PageRequest();

        // When
        Set<ConstraintViolation<PageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getPage()).isEqualTo(0);
        assertThat(request.getPageSize()).isEqualTo(20);
        assertThat(request.getSortOrder()).isEqualTo(PageRequest.SortOrder.ASC);
    }

    @Test
    @DisplayName("Page number cannot be negative")
    void testPageNumberCannotBeNegative() {
        // Given
        PageRequest request = new PageRequest(-1, 20, null, PageRequest.SortOrder.ASC);

        // When
        Set<ConstraintViolation<PageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Page number must be non-negative");
    }

    @Test
    @DisplayName("Page size must be at least 1")
    void testPageSizeTooSmall() {
        // Given
        PageRequest request = new PageRequest(0, 0, null, PageRequest.SortOrder.ASC);

        // When
        Set<ConstraintViolation<PageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Page size must be at least 1");
    }

    @Test
    @DisplayName("Page size must not exceed 100")
    void testPageSizeTooLarge() {
        // Given
        PageRequest request = new PageRequest(0, 101, null, PageRequest.SortOrder.ASC);

        // When
        Set<ConstraintViolation<PageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Page size must not exceed 100");
    }

    @Test
    @DisplayName("Page size at maximum (100) should be valid")
    void testPageSizeAtMaximum() {
        // Given
        PageRequest request = new PageRequest(0, 100, null, PageRequest.SortOrder.ASC);

        // When
        Set<ConstraintViolation<PageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Optional fields can be null")
    void testOptionalFieldsCanBeNull() {
        // Given
        PageRequest request = new PageRequest(0, 20, null, null);

        // When
        Set<ConstraintViolation<PageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}
