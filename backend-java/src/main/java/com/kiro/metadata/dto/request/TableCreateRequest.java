package com.kiro.metadata.dto.request;

import com.kiro.metadata.entity.TableType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表创建请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableCreateRequest {
    
    @NotBlank(message = "Database name cannot be blank")
    @Size(min = 1, max = 100, message = "Database name length must be between 1-100")
    private String databaseName;
    
    @NotBlank(message = "Table name cannot be blank")
    @Size(min = 1, max = 100, message = "Table name length must be between 1-100")
    private String tableName;
    
    @NotNull(message = "Table type cannot be null")
    private TableType tableType;
    
    @Size(max = 1000, message = "Description length cannot exceed 1000")
    private String description;
    
    @Size(max = 50, message = "Storage format length cannot exceed 50")
    private String storageFormat;
    
    @Size(max = 500, message = "Storage location length cannot exceed 500")
    private String storageLocation;
    
    @Min(value = 0, message = "Data size cannot be negative")
    private Long dataSizeBytes;
}
