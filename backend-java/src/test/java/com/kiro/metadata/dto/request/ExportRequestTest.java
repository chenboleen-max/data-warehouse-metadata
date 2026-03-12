package com.kiro.metadata.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExportRequest DTO
 */
class ExportRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidExportRequest() {
        ExportRequest request = ExportRequest.builder()
                .format("CSV")
                .databaseName("analytics")
                .tableType("TABLE")
                .includeColumns(true)
                .includeQualityMetrics(false)
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testMinimalValidRequest() {
        ExportRequest request = ExportRequest.builder()
                .format("JSON")
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        
        // Check defaults
        assertThat(request.getIncludeColumns()).isTrue();
        assertThat(request.getIncludeQualityMetrics()).isFalse();
    }

    @Test
    void testBlankFormatFails() {
        ExportRequest request = ExportRequest.builder()
                .format("")
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("cannot be blank");
    }

    @Test
    void testWithTableIds() {
        ExportRequest request = ExportRequest.builder()
                .format("CSV")
                .tableIds(Arrays.asList("table1", "table2", "table3"))
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getTableIds()).hasSize(3);
    }

    @Test
    void testWithCatalogIds() {
        ExportRequest request = ExportRequest.builder()
                .format("JSON")
                .catalogIds(Arrays.asList("cat1", "cat2"))
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getCatalogIds()).hasSize(2);
    }

    @Test
    void testWithFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("minRecordCount", 1000);
        filters.put("maxNullRate", 0.1);

        ExportRequest request = ExportRequest.builder()
                .format("CSV")
                .filters(filters)
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getFilters()).hasSize(2);
    }

    @Test
    void testWithAllOptions() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("owner", "john");

        ExportRequest request = ExportRequest.builder()
                .format("CSV")
                .databaseName("analytics")
                .tableType("TABLE")
                .tableIds(Arrays.asList("t1", "t2"))
                .catalogIds(Arrays.asList("c1"))
                .filters(filters)
                .includeColumns(true)
                .includeQualityMetrics(true)
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getIncludeColumns()).isTrue();
        assertThat(request.getIncludeQualityMetrics()).isTrue();
    }

    @Test
    void testColumnsOnlyExport() {
        ExportRequest request = ExportRequest.builder()
                .format("JSON")
                .includeColumns(true)
                .includeQualityMetrics(false)
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testWithoutColumns() {
        ExportRequest request = ExportRequest.builder()
                .format("CSV")
                .includeColumns(false)
                .build();

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getIncludeColumns()).isFalse();
    }
}
