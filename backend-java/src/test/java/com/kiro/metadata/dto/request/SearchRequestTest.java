package com.kiro.metadata.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SearchRequest DTO
 */
class SearchRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidSearchRequest() {
        SearchRequest request = SearchRequest.builder()
                .keyword("user")
                .databaseName("analytics")
                .tableType("TABLE")
                .sortBy("relevance")
                .sortOrder("desc")
                .page(0)
                .pageSize(20)
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testMinimalValidRequest() {
        SearchRequest request = SearchRequest.builder()
                .keyword("test")
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        
        // Check defaults
        assertThat(request.getSortBy()).isEqualTo("relevance");
        assertThat(request.getSortOrder()).isEqualTo("desc");
        assertThat(request.getPage()).isEqualTo(0);
        assertThat(request.getPageSize()).isEqualTo(20);
    }

    @Test
    void testBlankKeywordFails() {
        SearchRequest request = SearchRequest.builder()
                .keyword("")
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("cannot be blank");
    }

    @Test
    void testKeywordTooLongFails() {
        String longKeyword = "a".repeat(201);
        SearchRequest request = SearchRequest.builder()
                .keyword(longKeyword)
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 200 characters");
    }

    @Test
    void testNegativePageFails() {
        SearchRequest request = SearchRequest.builder()
                .keyword("test")
                .page(-1)
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be at least 0");
    }

    @Test
    void testPageSizeTooSmallFails() {
        SearchRequest request = SearchRequest.builder()
                .keyword("test")
                .pageSize(0)
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be at least 1");
    }

    @Test
    void testPageSizeTooLargeFails() {
        SearchRequest request = SearchRequest.builder()
                .keyword("test")
                .pageSize(101)
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 100");
    }

    @Test
    void testWithFilters() {
        SearchRequest request = SearchRequest.builder()
                .keyword("user")
                .databaseName("analytics")
                .tableType("TABLE")
                .ownerUsername("john")
                .catalogIds(Arrays.asList("cat1", "cat2"))
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getCatalogIds()).hasSize(2);
    }

    @Test
    void testCustomSortAndPagination() {
        SearchRequest request = SearchRequest.builder()
                .keyword("test")
                .sortBy("updated_at")
                .sortOrder("asc")
                .page(5)
                .pageSize(50)
                .build();

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.getSortBy()).isEqualTo("updated_at");
        assertThat(request.getSortOrder()).isEqualTo("asc");
        assertThat(request.getPage()).isEqualTo(5);
        assertThat(request.getPageSize()).isEqualTo(50);
    }
}
