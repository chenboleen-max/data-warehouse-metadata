package com.kiro.metadata.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表更新请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableUpdateRequest {
    
    @Size(max = 1000, message = "Description length cannot exceed 1000")
    private String description;
    
    @Size(max = 50, message = "Storage format length cannot exceed 50")
    private String storageFormat;
    
    @Size(max = 500, message = "Storage location length cannot exceed 500")
    private String storageLocation;
    
    @Min(value = 0, message = "Data size cannot be negative")
    private Long dataSizeBytes;
}
