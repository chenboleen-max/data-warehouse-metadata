package com.kiro.metadata.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ImportRequest DTO
 */
class ImportRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidImportRequest() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("table_name", "tableName");
        mapping.put("db_name", "databaseName");

        ImportRequest request = ImportRequest.builder()
                .format("CSV")
                .fileContent("table_name,db_name\nusers,analytics")
                .fieldMapping(mapping)
                .skipErrors(false)
                .updateExisting(false)
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testMinimalValidRequest() {
        ImportRequest request = ImportRequest.builder()
                .format("JSON")
                .fileContent("{\"tables\": []}")
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        
        // Check defaults
        assertThat(request.getSkipErrors()).isFalse();
        assertThat(request.getUpdateExisting()).isFalse();
    }

    @Test
    void testBlankFormatFails() {
        ImportRequest request = ImportRequest.builder()
                .format("")
                .fileContent("content")
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("cannot be blank");
    }

    @Test
    void testBlankFileContentFails() {
        ImportRequest request = ImportRequest.builder()
                .format("CSV")
                .fileContent("")
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("cannot be blank");
    }

    @Test
    void testWithFieldMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("name", "tableName");
        mapping.put("desc", "description");
        mapping.put("type", "tableType");

        ImportRequest request = ImportRequest.builder()
                .format("CSV")
                .fileContent("name,desc,type\nusers,User table,TABLE")
                .fieldMapping(mapping)
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getFieldMapping()).hasSize(3);
    }

    @Test
    void testWithSkipErrors() {
        ImportRequest request = ImportRequest.builder()
                .format("CSV")
                .fileContent("content")
                .skipErrors(true)
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getSkipErrors()).isTrue();
    }

    @Test
    void testWithUpdateExisting() {
        ImportRequest request = ImportRequest.builder()
                .format("JSON")
                .fileContent("{}")
                .updateExisting(true)
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getUpdateExisting()).isTrue();
    }

    @Test
    void testNullFieldMappingIsValid() {
        ImportRequest request = ImportRequest.builder()
                .format("CSV")
                .fileContent("content")
                .fieldMapping(null)
                .build();

        Set<ConstraintViolation<ImportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
