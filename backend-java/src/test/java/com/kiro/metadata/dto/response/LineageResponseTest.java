package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.LineageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LineageResponse DTO
 * 
 * Validates: Requirements 3.1, 3.5 (Lineage Relationship Management)
 */
@DisplayName("LineageResponse Tests")
class LineageResponseTest {

    @Test
    @DisplayName("Should create lineage response with all fields")
    void testCreateLineageResponseWithAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        UUID sourceTableId = UUID.randomUUID();
        UUID targetTableId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        LineageResponse response = LineageResponse.builder()
            .id(id)
            .sourceTableId(sourceTableId)
            .sourceTableName("db1.source_table")
            .targetTableId(targetTableId)
            .targetTableName("db2.target_table")
            .lineageType(LineageType.DIRECT)
            .transformationLogic("INSERT INTO target SELECT * FROM source")
            .createdAt(now)
            .updatedAt(now)
            .createdBy(createdBy)
            .createdByUsername("testuser")
            .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getSourceTableId()).isEqualTo(sourceTableId);
        assertThat(response.getSourceTableName()).isEqualTo("db1.source_table");
        assertThat(response.getTargetTableId()).isEqualTo(targetTableId);
        assertThat(response.getTargetTableName()).isEqualTo("db2.target_table");
        assertThat(response.getLineageType()).isEqualTo(LineageType.DIRECT);
        assertThat(response.getTransformationLogic()).isEqualTo("INSERT INTO target SELECT * FROM source");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
        assertThat(response.getCreatedBy()).isEqualTo(createdBy);
        assertThat(response.getCreatedByUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should create lineage response with minimal fields")
    void testCreateLineageResponseWithMinimalFields() {
        // Given
        UUID id = UUID.randomUUID();
        UUID sourceTableId = UUID.randomUUID();
        UUID targetTableId = UUID.randomUUID();

        // When
        LineageResponse response = LineageResponse.builder()
            .id(id)
            .sourceTableId(sourceTableId)
            .targetTableId(targetTableId)
            .lineageType(LineageType.INDIRECT)
            .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getSourceTableId()).isEqualTo(sourceTableId);
        assertThat(response.getTargetTableId()).isEqualTo(targetTableId);
        assertThat(response.getLineageType()).isEqualTo(LineageType.INDIRECT);
        assertThat(response.getTransformationLogic()).isNull();
        assertThat(response.getSourceTableName()).isNull();
        assertThat(response.getTargetTableName()).isNull();
    }

    @Test
    @DisplayName("Should support DIRECT lineage type")
    void testDirectLineageType() {
        // When
        LineageResponse response = LineageResponse.builder()
            .id(UUID.randomUUID())
            .sourceTableId(UUID.randomUUID())
            .targetTableId(UUID.randomUUID())
            .lineageType(LineageType.DIRECT)
            .build();

        // Then
        assertThat(response.getLineageType()).isEqualTo(LineageType.DIRECT);
    }

    @Test
    @DisplayName("Should support INDIRECT lineage type")
    void testIndirectLineageType() {
        // When
        LineageResponse response = LineageResponse.builder()
            .id(UUID.randomUUID())
            .sourceTableId(UUID.randomUUID())
            .targetTableId(UUID.randomUUID())
            .lineageType(LineageType.INDIRECT)
            .build();

        // Then
        assertThat(response.getLineageType()).isEqualTo(LineageType.INDIRECT);
    }

    @Test
    @DisplayName("Should handle null transformation logic")
    void testNullTransformationLogic() {
        // When
        LineageResponse response = LineageResponse.builder()
            .id(UUID.randomUUID())
            .sourceTableId(UUID.randomUUID())
            .targetTableId(UUID.randomUUID())
            .lineageType(LineageType.DIRECT)
            .transformationLogic(null)
            .build();

        // Then
        assertThat(response.getTransformationLogic()).isNull();
    }

    @Test
    @DisplayName("Should handle long transformation logic")
    void testLongTransformationLogic() {
        // Given
        String longLogic = "SELECT * FROM source WHERE condition = 'value' ".repeat(100);

        // When
        LineageResponse response = LineageResponse.builder()
            .id(UUID.randomUUID())
            .sourceTableId(UUID.randomUUID())
            .targetTableId(UUID.randomUUID())
            .lineageType(LineageType.DIRECT)
            .transformationLogic(longLogic)
            .build();

        // Then
        assertThat(response.getTransformationLogic()).isEqualTo(longLogic);
    }
}
