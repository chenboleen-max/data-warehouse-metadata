package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.Lineage;
import com.kiro.metadata.entity.LineageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * è¡ç¼å³ç³»ååº?DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageResponse {
    
    private String id;
    private String sourceTableId;
    private String sourceTableName;
    private String targetTableId;
    private String targetTableName;
    private LineageType lineageType;
    private String transformationLogic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse createdBy;
    
    /**
     * ä»?Lineage å®ä½è½¬æ¢ä¸?LineageResponse
     */
    public static LineageResponse from(Lineage lineage) {
        return LineageResponse.builder()
                .id(lineage.getId())
                .sourceTableId(lineage.getSourceTable() != null ? lineage.getSourceTable().getId() : null)
                .sourceTableName(lineage.getSourceTable() != null ? 
                        lineage.getSourceTable().getDatabaseName() + "." + lineage.getSourceTable().getTableName() : null)
                .targetTableId(lineage.getTargetTable() != null ? lineage.getTargetTable().getId() : null)
                .targetTableName(lineage.getTargetTable() != null ? 
                        lineage.getTargetTable().getDatabaseName() + "." + lineage.getTargetTable().getTableName() : null)
                .lineageType(lineage.getLineageType())
                .transformationLogic(lineage.getTransformationLogic())
                .createdAt(lineage.getCreatedAt())
                .updatedAt(lineage.getUpdatedAt())
                .createdBy(lineage.getCreatedBy() != null ? UserResponse.from(lineage.getCreatedBy()) : null)
                .build();
    }
}

