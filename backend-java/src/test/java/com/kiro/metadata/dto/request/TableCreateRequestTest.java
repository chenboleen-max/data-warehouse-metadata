package com.kiro.metadata.dto.request;

import com.kiro.metadata.entity.TableType;
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
 * Unit tests for TableCreateRequest DTO validation
 * 
 * Validates: Requirements 1.1, 2.1 (Table Metadata Management)
 */
@DisplayName("TableCreateRequest Validation Tests")
class TableCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid table create request should pass validation")
    void testValidTableCreateRequest() {
        // Given
        TableCreateRequest request = new TableCreateRequest(
            "test_db",
            "test_table",
            TableType.TABLE,
            "Test table description",
            "PARQUET",
            "/data/warehouse/test_table",
            1024000L
        );

        // When
        Set<ConstraintViolation<TableCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Database name cannot be blank")
    void testDatabaseNameCannotBeBlank() {
        // Given
        TableCreateRequest request = new TableCreateRequest(
            "",
            "test_table",
            TableType.TABLE,
            null,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<TableCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> 
            v.getMessage().contains("Database name cannot be blank")
        );
    }

    @Test
    @DisplayName("Table name cannot be blank")
    void testTableNameCannotBeBlank() {
        // Given
        TableCreateRequest request = new TableCreateRequest(
            "test_db",
            "",
            TableType.TABLE,
            null,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<TableCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> 
            v.getMessage().contains("Table name cannot be blank")
        );
    }

    @Test
    @DisplayName("Table type cannot be null")
    void testTableTypeCannotBeNull() {
        // Given
        TableCreateRequest request = new TableCreateRequest(
            "test_db",
            "test_table",
            null,
            null,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<TableCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Table type cannot be null");
    }

    @Test
    @DisplayName("Database name must not exceed 100 characters")
    void testDatabaseNameTooLong() {
        // Given
        String longName = "a".repeat(101);
        TableCreateRequest request = new TableCreateRequest(
            longName,
            "test_table",
            TableType.TABLE,
            null,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<TableCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Database name must not exceed 100 characters");
    }

    @Test
    @DisplayName("Description must not exceed 1000 characters")
    void testDescriptionTooLong() {
        // Given
        String longDescription = "a".repeat(1001);
        TableCreateRequest request = new TableCreateRequest(
            "test_db",
            "test_table",
            TableType.TABLE,
            longDescription,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<TableCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Description must not exceed 1000 characters");
    }

    @Test
    @DisplayName("Optional fields can be null")
    void testOptionalFieldsCanBeNull() {
        // Given
        TableCreateRequest request = new TableCreateRequest(
            "test_db",
            "test_table",
            TableType.TABLE,
            null,
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<TableCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}
