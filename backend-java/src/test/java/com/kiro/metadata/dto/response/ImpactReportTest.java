package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ImpactReport DTO
 * 
 * Validates: Requirements 3.4 (Impact Analysis)
 */
@DisplayName("ImpactReport Tests")
class ImpactReportTest {

    @Test
    @DisplayName("Should create impact report with all fields")
    void testCreateImpactReportWithAllFields() {
        // Given
        List<UUID> affectedTables = Arrays.asList(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        ImpactReport report = ImpactReport.builder()
            .affectedTables(affectedTables)
            .maxDepth(3)
            .totalCount(3)
            .build();

        // Then
        assertThat(report.getAffectedTables()).hasSize(3);
        assertThat(report.getMaxDepth()).isEqualTo(3);
        assertThat(report.getTotalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should create empty impact report")
    void testCreateEmptyImpactReport() {
        // When
        ImpactReport report = ImpactReport.builder()
            .affectedTables(new ArrayList<>())
            .maxDepth(0)
            .totalCount(0)
            .build();

        // Then
        assertThat(report.getAffectedTables()).isEmpty();
        assertThat(report.getMaxDepth()).isEqualTo(0);
        assertThat(report.getTotalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should initialize with default empty list")
    void testDefaultEmptyList() {
        // When
        ImpactReport report = ImpactReport.builder().build();

        // Then
        assertThat(report.getAffectedTables()).isNotNull();
        assertThat(report.getAffectedTables()).isEmpty();
    }

    @Test
    @DisplayName("Total count should match affected tables size")
    void testTotalCountMatchesSize() {
        // Given
        List<UUID> affectedTables = Arrays.asList(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        ImpactReport report = ImpactReport.builder()
            .affectedTables(affectedTables)
            .maxDepth(2)
            .totalCount(affectedTables.size())
            .build();

        // Then
        assertThat(report.getTotalCount()).isEqualTo(affectedTables.size());
        assertThat(report.getTotalCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle single affected table")
    void testSingleAffectedTable() {
        // Given
        List<UUID> affectedTables = Arrays.asList(UUID.randomUUID());

        // When
        ImpactReport report = ImpactReport.builder()
            .affectedTables(affectedTables)
            .maxDepth(1)
            .totalCount(1)
            .build();

        // Then
        assertThat(report.getAffectedTables()).hasSize(1);
        assertThat(report.getMaxDepth()).isEqualTo(1);
        assertThat(report.getTotalCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle deep lineage impact")
    void testDeepLineageImpact() {
        // Given
        List<UUID> affectedTables = Arrays.asList(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        ImpactReport report = ImpactReport.builder()
            .affectedTables(affectedTables)
            .maxDepth(5)
            .totalCount(6)
            .build();

        // Then
        assertThat(report.getMaxDepth()).isEqualTo(5);
        assertThat(report.getTotalCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("Should allow null fields")
    void testNullFields() {
        // When
        ImpactReport report = new ImpactReport();

        // Then
        assertThat(report.getAffectedTables()).isNotNull();
        assertThat(report.getAffectedTables()).isEmpty();
        assertThat(report.getMaxDepth()).isNull();
        assertThat(report.getTotalCount()).isNull();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void testEquality() {
        // Given
        List<UUID> affectedTables = Arrays.asList(
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        ImpactReport report1 = ImpactReport.builder()
            .affectedTables(affectedTables)
            .maxDepth(2)
            .totalCount(2)
            .build();

        ImpactReport report2 = ImpactReport.builder()
            .affectedTables(affectedTables)
            .maxDepth(2)
            .totalCount(2)
            .build();

        // Then
        assertThat(report1).isEqualTo(report2);
        assertThat(report1.hashCode()).isEqualTo(report2.hashCode());
    }

    @Test
    @DisplayName("Should handle large number of affected tables")
    void testLargeNumberOfAffectedTables() {
        // Given
        List<UUID> affectedTables = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            affectedTables.add(UUID.randomUUID());
        }

        // When
        ImpactReport report = ImpactReport.builder()
            .affectedTables(affectedTables)
            .maxDepth(5)
            .totalCount(100)
            .build();

        // Then
        assertThat(report.getAffectedTables()).hasSize(100);
        assertThat(report.getTotalCount()).isEqualTo(100);
    }
}
