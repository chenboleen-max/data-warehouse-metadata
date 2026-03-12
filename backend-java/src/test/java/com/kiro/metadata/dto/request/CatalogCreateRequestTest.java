package com.kiro.metadata.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CatalogCreateRequest DTO
 */
class CatalogCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCatalogCreateRequest() {
        CatalogCreateRequest request = new CatalogCreateRequest(
                "User Domain",
                "Domain for user-related tables",
                UUID.randomUUID(),
                2
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidRootCatalog() {
        CatalogCreateRequest request = new CatalogCreateRequest(
                "Business Domain",
                "Root level business domain",
                null,
                1
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testBlankNameFails() {
        CatalogCreateRequest request = new CatalogCreateRequest(
                "",
                "Description",
                null,
                1
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("cannot be blank");
    }

    @Test
    void testNameTooLongFails() {
        String longName = "a".repeat(101);
        CatalogCreateRequest request = new CatalogCreateRequest(
                longName,
                "Description",
                null,
                1
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 100 characters");
    }

    @Test
    void testDescriptionTooLongFails() {
        String longDescription = "a".repeat(1001);
        CatalogCreateRequest request = new CatalogCreateRequest(
                "Valid Name",
                longDescription,
                null,
                1
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 1000 characters");
    }

    @Test
    void testLevelTooLowFails() {
        CatalogCreateRequest request = new CatalogCreateRequest(
                "Valid Name",
                "Description",
                null,
                0
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be at least 1");
    }

    @Test
    void testLevelTooHighFails() {
        CatalogCreateRequest request = new CatalogCreateRequest(
                "Valid Name",
                "Description",
                null,
                6
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 5");
    }

    @Test
    void testAllLevelsValid() {
        for (int level = 1; level <= 5; level++) {
            CatalogCreateRequest request = new CatalogCreateRequest(
                    "Level " + level,
                    "Description",
                    null,
                    level
            );

            Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    void testNullDescriptionIsValid() {
        CatalogCreateRequest request = new CatalogCreateRequest(
                "Valid Name",
                null,
                null,
                1
        );

        Set<ConstraintViolation<CatalogCreateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
