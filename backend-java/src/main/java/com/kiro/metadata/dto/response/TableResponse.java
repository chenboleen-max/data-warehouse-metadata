package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.entity.TableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

/**
 * 表响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableResponse {
    
    private String id;
    private String databaseName;
    private String tableName;
    private TableType tableType;
    private String description;
    private String storageFormat;
    private String storageLocation;
    private Long dataSizeBytes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAccessedAt;
    private UserResponse owner;
    private List<ColumnResponse> columns;
    
    /**
     * 从TableMetadata实体转换为TableResponse
     */
    public static TableResponse from(TableMetadata table) {
        return TableResponse.builder()
                .id(table.getId())
                .databaseName(table.getDatabaseName())
                .tableName(table.getTableName())
                .tableType(table.getTableType())
                .description(table.getDescription())
                .storageFormat(table.getStorageFormat())
                .storageLocation(table.getStorageLocation())
                .dataSizeBytes(table.getDataSizeBytes())
                .createdAt(table.getCreatedAt())
                .updatedAt(table.getUpdatedAt())
                .lastAccessedAt(table.getLastAccessedAt())
                .owner(table.getOwner() != null ? UserResponse.from(table.getOwner()) : null)
                .columns(table.getColumns() != null ? 
                        table.getColumns().stream()
                                .map(ColumnResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
    
    /**
     * 从TableMetadata实体转换为TableResponse（不包含字段列表）
     */
    public static TableResponse fromWithoutColumns(TableMetadata table) {
        return TableResponse.builder()
                .id(table.getId())
                .databaseName(table.getDatabaseName())
                .tableName(table.getTableName())
                .tableType(table.getTableType())
                .description(table.getDescription())
                .storageFormat(table.getStorageFormat())
                .storageLocation(table.getStorageLocation())
                .dataSizeBytes(table.getDataSizeBytes())
                .createdAt(table.getCreatedAt())
                .updatedAt(table.getUpdatedAt())
                .lastAccessedAt(table.getLastAccessedAt())
                .owner(table.getOwner() != null ? UserResponse.from(table.getOwner()) : null)
                .build();
    }
}

