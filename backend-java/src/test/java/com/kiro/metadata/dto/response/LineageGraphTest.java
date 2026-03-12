package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LineageGraph DTO
 * 
 * Validates: Requirements 3.2, 3.3 (Lineage Graph Visualization)
 */
@DisplayName("LineageGraph Tests")
class LineageGraphTest {

    @Test
    @DisplayName("Should create empty lineage graph")
    void testCreateEmptyLineageGraph() {
        // When
        LineageGraph graph = LineageGraph.builder().build();

        // Then
        assertThat(graph.getNodes()).isNotNull();
        assertThat(graph.getNodes()).isEmpty();
        assertThat(graph.getEdges()).isNotNull();
        assertThat(graph.getEdges()).isEmpty();
    }

    @Test
    @DisplayName("Should create lineage graph with nodes and edges")
    void testCreateLineageGraphWithNodesAndEdges() {
        // Given
        UUID rootId = UUID.randomUUID();
        UUID upstreamId = UUID.randomUUID();
        UUID downstreamId = UUID.randomUUID();

        LineageNode rootNode = LineageNode.builder()
            .id(rootId)
            .name("db.root_table")
            .depth(0)
            .type("root")
            .build();

        LineageNode upstreamNode = LineageNode.builder()
            .id(upstreamId)
            .name("db.upstream_table")
            .depth(1)
            .type("upstream")
            .build();

        LineageNode downstreamNode = LineageNode.builder()
            .id(downstreamId)
            .name("db.downstream_table")
            .depth(1)
            .type("downstream")
            .build();

        LineageEdge edge1 = LineageEdge.builder()
            .source(upstreamId)
            .target(rootId)
            .type("DIRECT")
            .build();

        LineageEdge edge2 = LineageEdge.builder()
            .source(rootId)
            .target(downstreamId)
            .type("INDIRECT")
            .build();

        // When
        LineageGraph graph = LineageGraph.builder()
            .nodes(Arrays.asList(rootNode, upstreamNode, downstreamNode))
            .edges(Arrays.asList(edge1, edge2))
            .build();

        // Then
        assertThat(graph.getNodes()).hasSize(3);
        assertThat(graph.getEdges()).hasSize(2);
        assertThat(graph.getNodes()).contains(rootNode, upstreamNode, downstreamNode);
        assertThat(graph.getEdges()).contains(edge1, edge2);
    }

    @Test
    @DisplayName("Should verify all edge endpoints exist in nodes")
    void testEdgeEndpointsExistInNodes() {
        // Given
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        LineageNode sourceNode = LineageNode.builder()
            .id(sourceId)
            .name("db.source")
            .depth(1)
            .type("upstream")
            .build();

        LineageNode targetNode = LineageNode.builder()
            .id(targetId)
            .name("db.target")
            .depth(0)
            .type("root")
            .build();

        LineageEdge edge = LineageEdge.builder()
            .source(sourceId)
            .target(targetId)
            .type("DIRECT")
            .build();

        LineageGraph graph = LineageGraph.builder()
            .nodes(Arrays.asList(sourceNode, targetNode))
            .edges(Arrays.asList(edge))
            .build();

        // When
        List<UUID> nodeIds = graph.getNodes().stream()
            .map(LineageNode::getId)
            .toList();

        // Then
        assertThat(nodeIds).contains(edge.getSource());
        assertThat(nodeIds).contains(edge.getTarget());
    }

    @Test
    @DisplayName("Should handle graph with only root node")
    void testGraphWithOnlyRootNode() {
        // Given
        LineageNode rootNode = LineageNode.builder()
            .id(UUID.randomUUID())
            .name("db.isolated_table")
            .depth(0)
            .type("root")
            .build();

        // When
        LineageGraph graph = LineageGraph.builder()
            .nodes(Arrays.asList(rootNode))
            .edges(new ArrayList<>())
            .build();

        // Then
        assertThat(graph.getNodes()).hasSize(1);
        assertThat(graph.getEdges()).isEmpty();
    }

    @Test
    @DisplayName("Should support multi-level upstream lineage")
    void testMultiLevelUpstreamLineage() {
        // Given
        UUID rootId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();

        LineageNode rootNode = LineageNode.builder()
            .id(rootId)
            .name("db.root")
            .depth(0)
            .type("root")
            .build();

        LineageNode level1Node = LineageNode.builder()
            .id(level1Id)
            .name("db.level1")
            .depth(1)
            .type("upstream")
            .build();

        LineageNode level2Node = LineageNode.builder()
            .id(level2Id)
            .name("db.level2")
            .depth(2)
            .type("upstream")
            .build();

        LineageEdge edge1 = LineageEdge.builder()
            .source(level1Id)
            .target(rootId)
            .type("DIRECT")
            .build();

        LineageEdge edge2 = LineageEdge.builder()
            .source(level2Id)
            .target(level1Id)
            .type("DIRECT")
            .build();

        // When
        LineageGraph graph = LineageGraph.builder()
            .nodes(Arrays.asList(rootNode, level1Node, level2Node))
            .edges(Arrays.asList(edge1, edge2))
            .build();

        // Then
        assertThat(graph.getNodes()).hasSize(3);
        assertThat(graph.getEdges()).hasSize(2);
        assertThat(graph.getNodes().stream().map(LineageNode::getDepth).max(Integer::compareTo))
            .hasValue(2);
    }

    @Test
    @DisplayName("Should support multi-level downstream lineage")
    void testMultiLevelDownstreamLineage() {
        // Given
        UUID rootId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();

        LineageNode rootNode = LineageNode.builder()
            .id(rootId)
            .name("db.root")
            .depth(0)
            .type("root")
            .build();

        LineageNode level1Node = LineageNode.builder()
            .id(level1Id)
            .name("db.level1")
            .depth(1)
            .type("downstream")
            .build();

        LineageNode level2Node = LineageNode.builder()
            .id(level2Id)
            .name("db.level2")
            .depth(2)
            .type("downstream")
            .build();

        LineageEdge edge1 = LineageEdge.builder()
            .source(rootId)
            .target(level1Id)
            .type("DIRECT")
            .build();

        LineageEdge edge2 = LineageEdge.builder()
            .source(level1Id)
            .target(level2Id)
            .type("INDIRECT")
            .build();

        // When
        LineageGraph graph = LineageGraph.builder()
            .nodes(Arrays.asList(rootNode, level1Node, level2Node))
            .edges(Arrays.asList(edge1, edge2))
            .build();

        // Then
        assertThat(graph.getNodes()).hasSize(3);
        assertThat(graph.getEdges()).hasSize(2);
        assertThat(graph.getNodes().stream()
            .filter(n -> "downstream".equals(n.getType()))
            .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should support bidirectional lineage graph")
    void testBidirectionalLineageGraph() {
        // Given
        UUID rootId = UUID.randomUUID();
        UUID upstreamId = UUID.randomUUID();
        UUID downstreamId = UUID.randomUUID();

        List<LineageNode> nodes = Arrays.asList(
            LineageNode.builder().id(rootId).name("db.root").depth(0).type("root").build(),
            LineageNode.builder().id(upstreamId).name("db.upstream").depth(1).type("upstream").build(),
            LineageNode.builder().id(downstreamId).name("db.downstream").depth(1).type("downstream").build()
        );

        List<LineageEdge> edges = Arrays.asList(
            LineageEdge.builder().source(upstreamId).target(rootId).type("DIRECT").build(),
            LineageEdge.builder().source(rootId).target(downstreamId).type("DIRECT").build()
        );

        // When
        LineageGraph graph = LineageGraph.builder()
            .nodes(nodes)
            .edges(edges)
            .build();

        // Then
        assertThat(graph.getNodes()).hasSize(3);
        assertThat(graph.getEdges()).hasSize(2);
        assertThat(graph.getNodes().stream().filter(n -> "upstream".equals(n.getType())).count()).isEqualTo(1);
        assertThat(graph.getNodes().stream().filter(n -> "downstream".equals(n.getType())).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should support equality comparison")
    void testEquality() {
        // Given
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        LineageNode node = LineageNode.builder()
            .id(sourceId)
            .name("db.table")
            .depth(0)
            .type("root")
            .build();

        LineageEdge edge = LineageEdge.builder()
            .source(sourceId)
            .target(targetId)
            .type("DIRECT")
            .build();

        LineageGraph graph1 = LineageGraph.builder()
            .nodes(Arrays.asList(node))
            .edges(Arrays.asList(edge))
            .build();

        LineageGraph graph2 = LineageGraph.builder()
            .nodes(Arrays.asList(node))
            .edges(Arrays.asList(edge))
            .build();

        // Then
        assertThat(graph1).isEqualTo(graph2);
        assertThat(graph1.hashCode()).isEqualTo(graph2.hashCode());
    }
}
