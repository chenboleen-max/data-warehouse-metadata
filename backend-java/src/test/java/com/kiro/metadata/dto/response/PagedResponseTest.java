package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PagedResponse DTO
 * 
 * Validates: Requirements 1.4 (Pagination Support)
 */
@DisplayName("PagedResponse Tests")
class PagedResponseTest {

    @Test
    @DisplayName("Should correctly identify if there is a next page")
    void testHasNext() {
        // Given - page 0 of 3 total pages
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.<String>builder()
            .items(items)
            .total(60L)
            .page(0)
            .pageSize(20)
            .totalPages(3)
            .build();

        // Then
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Should correctly identify if there is no next page")
    void testHasNoNext() {
        // Given - last page
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.<String>builder()
            .items(items)
            .total(60L)
            .page(2)
            .pageSize(20)
            .totalPages(3)
            .build();

        // Then
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify if there is a previous page")
    void testHasPrevious() {
        // Given - page 1 of 3 total pages
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.<String>builder()
            .items(items)
            .total(60L)
            .page(1)
            .pageSize(20)
            .totalPages(3)
            .build();

        // Then
        assertThat(response.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should correctly identify if there is no previous page")
    void testHasNoPrevious() {
        // Given - first page
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.<String>builder()
            .items(items)
            .total(60L)
            .page(0)
            .pageSize(20)
            .totalPages(3)
            .build();

        // Then
        assertThat(response.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify first page")
    void testIsFirst() {
        // Given - first page
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.<String>builder()
            .items(items)
            .total(60L)
            .page(0)
            .pageSize(20)
            .totalPages(3)
            .build();

        // Then
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify last page")
    void testIsLast() {
        // Given - last page
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.<String>builder()
            .items(items)
            .total(60L)
            .page(2)
            .pageSize(20)
            .totalPages(3)
            .build();

        // Then
        assertThat(response.isLast()).isTrue();
        assertThat(response.isFirst()).isFalse();
    }

    @Test
    @DisplayName("Should handle single page correctly")
    void testSinglePage() {
        // Given - only one page
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.<String>builder()
            .items(items)
            .total(2L)
            .page(0)
            .pageSize(20)
            .totalPages(1)
            .build();

        // Then
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isFalse();
    }
}
