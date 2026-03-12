package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LineageNode DTO
 * 
 * Validates: Requirements 3.2, 3.3 (Lineage Graph Visualization)
 */
@DisplayName("LineageNode Tests")
class LineageNodeTest {

    @Test
    @DisplayName("Should create lineage node with all fields")
    void testCreateLineageNodeWithAllFields() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        LineageNode node = LineageNode.builder()
            .id(id)
            .name("warehouse.user_orders")
            .depth(2)
            .type("upstream")
            .build();

        // Then
        assertThat(node.getId()).isEqualTo(id);
        assertThat(node.getName()).isEqualTo("warehouse.user_orders");
        assertThat(node.getDepth()).isEqualTo(2);
        assertThat(node.getType()).isEqualTo("upstream");
    }

    @Test
    @DisplayName("Should create root node with depth 0")
    void testCreateRootNode() {
        // When
        LineageNode node = LineageNode.builder()
            .id(UUID.randomUUID())
            .name("db.root_table")
            .depth(0)
            .type("root")
            .build();

        // Then
        assertThat(node.getDepth()).isEqualTo(0);
        assertThat(node.getType()).isEqualTo("root");
    }

    @Test
    @DisplayName("Should create upstream node")
    void testCreateUpstreamNode() {
        // When
        LineageNode node = LineageNode.builder()
            .id(UUID.randomUUID())
            .name("db.upstream_table")
            .depth(1)
            .type("upstream")
            .build();

        // Then
        assertThat(node.getType()).isEqualTo("upstream");
        assertThat(node.getDepth()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should create downstream node")
    void testCreateDownstreamNode() {
        // When
        LineageNode node = LineageNode.builder()
            .id(UUID.randomUUID())
            .name("db.downstream_table")
            .depth(3)
            .type("downstream")
            .build();

        // Then
        assertThat(node.getType()).isEqualTo("downstream");
        assertThat(node.getDepth()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should support depth up to 5 levels")
    void testMaxDepth() {
        // When
        LineageNode node = LineageNode.builder()
            .id(UUID.randomUUID())
            .name("db.deep_table")
            .depth(5)
            .type("downstream")
            .build();

        // Then
        assertThat(node.getDepth()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle table name in database.table format")
    void testTableNameFormat() {
        // When
        LineageNode node = LineageNode.builder()
            .id(UUID.randomUUID())
            .name("production.sales_fact")
            .depth(1)
            .type("upstream")
            .build();

        // Then
        assertThat(node.getName()).contains(".");
        assertThat(node.getName().split("\\.")).hasSize(2);
    }

    @Test
    @DisplayName("Should allow null fields")
    void testNullFields() {
        // When
        LineageNode node = new LineageNode();

        // Then
        assertThat(node.getId()).isNull();
        assertThat(node.getName()).isNull();
        assertThat(node.getDepth()).isNull();
        assertThat(node.getType()).isNull();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void testEquality() {
        // Given
        UUID id = UUID.randomUUID();
        LineageNode node1 = LineageNode.builder()
            .id(id)
            .name("db.table")
            .depth(1)
            .type("upstream")
            .build();

        LineageNode node2 = LineageNode.builder()
            .id(id)
            .name("db.table")
            .depth(1)
            .type("upstream")
            .build();

        // Then
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }
}
