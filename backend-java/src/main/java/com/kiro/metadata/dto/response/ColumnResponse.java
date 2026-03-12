package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.ColumnMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 瀛楁鍝嶅簲 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnResponse {
    
    private String id;
    private String tableId;
    private String columnName;
    private String dataType;
    private Integer columnOrder;
    private Boolean isNullable;
    private Boolean isPartitionKey;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 浠?ColumnMetadata 瀹炰綋杞崲涓?ColumnResponse
     */
    public static ColumnResponse from(ColumnMetadata column) {
        return ColumnResponse.builder()
                .id(column.getId())
                .tableId(column.getTable() != null ? column.getTable().getId() : null)
                .columnName(column.getColumnName())
                .dataType(column.getDataType())
                .columnOrder(column.getColumnOrder())
                .isNullable(column.getIsNullable())
                .isPartitionKey(column.getIsPartitionKey())
                .description(column.getDescription())
                .createdAt(column.getCreatedAt())
                .updatedAt(column.getUpdatedAt())
                .build();
    }
}

