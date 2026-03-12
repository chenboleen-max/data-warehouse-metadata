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
 * 琛ㄥ搷搴?DTO
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
     * 浠?TableMetadata 瀹炰綋杞崲涓?TableResponse
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
     * 浠?TableMetadata 瀹炰綋杞崲涓?TableResponse锛堜笉鍖呭惈瀛楁鍒楄〃锛?
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

