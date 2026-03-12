package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LineageEdge DTO
 * 
 * Validates: Requirements 3.2 (Lineage Graph Visualization)
 */
@DisplayName("LineageEdge Tests")
class LineageEdgeTest {

    @Test
    @DisplayName("Should create lineage edge with all fields")
    void testCreateLineageEdgeWithAllFields() {
        // Given
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        // When
        LineageEdge edge = LineageEdge.builder()
            .source(sourceId)
            .target(targetId)
            .type("DIRECT")
            .build();

        // Then
        assertThat(edge.getSource()).isEqualTo(sourceId);
        assertThat(edge.getTarget()).isEqualTo(targetId);
        assertThat(edge.getType()).isEqualTo("DIRECT");
    }

    @Test
    @DisplayName("Should support DIRECT edge type")
    void testDirectEdgeType() {
        // When
        LineageEdge edge = LineageEdge.builder()
            .source(UUID.randomUUID())
            .target(UUID.randomUUID())
            .type("DIRECT")
            .build();

        // Then
        assertThat(edge.getType()).isEqualTo("DIRECT");
    }

    @Test
    @DisplayName("Should support INDIRECT edge type")
    void testIndirectEdgeType() {
        // When
        LineageEdge edge = LineageEdge.builder()
            .source(UUID.randomUUID())
            .target(UUID.randomUUID())
            .type("INDIRECT")
            .build();

        // Then
        assertThat(edge.getType()).isEqualTo("INDIRECT");
    }

    @Test
    @DisplayName("Should allow null fields")
    void testNullFields() {
        // When
        LineageEdge edge = new LineageEdge();

        // Then
        assertThat(edge.getSource()).isNull();
        assertThat(edge.getTarget()).isNull();
        assertThat(edge.getType()).isNull();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void testEquality() {
        // Given
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        LineageEdge edge1 = LineageEdge.builder()
            .source(sourceId)
            .target(targetId)
            .type("DIRECT")
            .build();

        LineageEdge edge2 = LineageEdge.builder()
            .source(sourceId)
            .target(targetId)
            .type("DIRECT")
            .build();

        // Then
        assertThat(edge1).isEqualTo(edge2);
        assertThat(edge1.hashCode()).isEqualTo(edge2.hashCode());
    }

    @Test
    @DisplayName("Should differentiate edges with different sources")
    void testDifferentSources() {
        // Given
        UUID targetId = UUID.randomUUID();

        LineageEdge edge1 = LineageEdge.builder()
            .source(UUID.randomUUID())
            .target(targetId)
            .type("DIRECT")
            .build();

        LineageEdge edge2 = LineageEdge.builder()
            .source(UUID.randomUUID())
            .target(targetId)
            .type("DIRECT")
            .build();

        // Then
        assertThat(edge1).isNotEqualTo(edge2);
    }

    @Test
    @DisplayName("Should differentiate edges with different targets")
    void testDifferentTargets() {
        // Given
        UUID sourceId = UUID.randomUUID();

        LineageEdge edge1 = LineageEdge.builder()
            .source(sourceId)
            .target(UUID.randomUUID())
            .type("DIRECT")
            .build();

        LineageEdge edge2 = LineageEdge.builder()
            .source(sourceId)
            .target(UUID.randomUUID())
            .type("DIRECT")
            .build();

        // Then
        assertThat(edge1).isNotEqualTo(edge2);
    }

    @Test
    @DisplayName("Should differentiate edges with different types")
    void testDifferentTypes() {
        // Given
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        LineageEdge edge1 = LineageEdge.builder()
            .source(sourceId)
            .target(targetId)
            .type("DIRECT")
            .build();

        LineageEdge edge2 = LineageEdge.builder()
            .source(sourceId)
            .target(targetId)
            .type("INDIRECT")
            .build();

        // Then
        assertThat(edge1).isNotEqualTo(edge2);
    }
}
