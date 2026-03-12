package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SearchResponse DTO
 */
class SearchResponseTest {

    @Test
    void testSearchResponseCreation() {
        TableResponse table1 = TableResponse.builder()
                .tableName("users")
                .databaseName("analytics")
                .build();

        SearchResponse.SearchResultItem item1 = SearchResponse.SearchResultItem.builder()
                .table(table1)
                .score(0.95)
                .matchedFields(Arrays.asList("tableName", "description"))
                .highlights(new HashMap<>())
                .build();

        PagedResponse<SearchResponse.SearchResultItem> pagedResults = PagedResponse.<SearchResponse.SearchResultItem>builder()
                .items(Arrays.asList(item1))
                .total(1L)
                .page(0)
                .pageSize(20)
                .totalPages(1)
                .build();

        Map<String, Object> filters = new HashMap<>();
        filters.put("databaseName", "analytics");

        SearchResponse response = SearchResponse.builder()
                .results(pagedResults)
                .keyword("user")
                .executionTimeMs(150L)
                .appliedFilters(filters)
                .build();

        assertThat(response.getKeyword()).isEqualTo("user");
        assertThat(response.getExecutionTimeMs()).isEqualTo(150L);
        assertThat(response.getResults().getItems()).hasSize(1);
        assertThat(response.getAppliedFilters()).containsKey("databaseName");
    }

    @Test
    void testSearchResultItemWithHighlights() {
        Map<String, List<String>> highlights = new HashMap<>();
        highlights.put("tableName", Arrays.asList("<em>user</em>s"));
        highlights.put("description", Arrays.asList("Table for <em>user</em> data"));

        SearchResponse.SearchResultItem item = SearchResponse.SearchResultItem.builder()
                .table(TableResponse.builder().tableName("users").build())
                .score(0.85)
                .matchedFields(Arrays.asList("tableName", "description"))
                .highlights(highlights)
                .build();

        assertThat(item.getHighlights()).hasSize(2);
        assertThat(item.getHighlights().get("tableName")).contains("<em>user</em>s");
        assertThat(item.getMatchedFields()).contains("tableName", "description");
    }

    @Test
    void testMultipleSearchResults() {
        SearchResponse.SearchResultItem item1 = SearchResponse.SearchResultItem.builder()
                .table(TableResponse.builder().tableName("users").build())
                .score(0.95)
                .build();

        SearchResponse.SearchResultItem item2 = SearchResponse.SearchResultItem.builder()
                .table(TableResponse.builder().tableName("user_events").build())
                .score(0.80)
                .build();

        SearchResponse.SearchResultItem item3 = SearchResponse.SearchResultItem.builder()
                .table(TableResponse.builder().tableName("user_profiles").build())
                .score(0.75)
                .build();

        PagedResponse<SearchResponse.SearchResultItem> pagedResults = PagedResponse.<SearchResponse.SearchResultItem>builder()
                .items(Arrays.asList(item1, item2, item3))
                .total(3L)
                .page(0)
                .pageSize(20)
                .totalPages(1)
                .build();

        SearchResponse response = SearchResponse.builder()
                .results(pagedResults)
                .keyword("user")
                .executionTimeMs(200L)
                .build();

        assertThat(response.getResults().getItems()).hasSize(3);
        assertThat(response.getResults().getItems().get(0).getScore()).isGreaterThan(
                response.getResults().getItems().get(1).getScore());
    }

    @Test
    void testEmptySearchResults() {
        PagedResponse<SearchResponse.SearchResultItem> pagedResults = PagedResponse.<SearchResponse.SearchResultItem>builder()
                .items(Arrays.asList())
                .total(0L)
                .page(0)
                .pageSize(20)
                .totalPages(0)
                .build();

        SearchResponse response = SearchResponse.builder()
                .results(pagedResults)
                .keyword("nonexistent")
                .executionTimeMs(50L)
                .build();

        assertThat(response.getResults().getItems()).isEmpty();
        assertThat(response.getResults().getTotal()).isZero();
    }

    @Test
    void testSearchWithFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("databaseName", "analytics");
        filters.put("tableType", "TABLE");
        filters.put("minRecordCount", 1000);

        SearchResponse response = SearchResponse.builder()
                .results(PagedResponse.<SearchResponse.SearchResultItem>builder()
                        .items(Arrays.asList())
                        .total(0L)
                        .page(0)
                        .pageSize(20)
                        .totalPages(0)
                        .build())
                .keyword("test")
                .executionTimeMs(100L)
                .appliedFilters(filters)
                .build();

        assertThat(response.getAppliedFilters()).hasSize(3);
        assertThat(response.getAppliedFilters().get("databaseName")).isEqualTo("analytics");
    }

    @Test
    void testFastSearchExecution() {
        SearchResponse response = SearchResponse.builder()
                .results(PagedResponse.<SearchResponse.SearchResultItem>builder()
                        .items(Arrays.asList())
                        .total(0L)
                        .page(0)
                        .pageSize(20)
                        .totalPages(0)
                        .build())
                .keyword("test")
                .executionTimeMs(50L)
                .build();

        // Requirement 4.2: Search should return results within 1 second (1000ms)
        assertThat(response.getExecutionTimeMs()).isLessThan(1000L);
    }

    @Test
    void testSearchResultItemScoreRange() {
        SearchResponse.SearchResultItem item = SearchResponse.SearchResultItem.builder()
                .table(TableResponse.builder().tableName("test").build())
                .score(0.5)
                .build();

        assertThat(item.getScore()).isBetween(0.0, 1.0);
    }
}
