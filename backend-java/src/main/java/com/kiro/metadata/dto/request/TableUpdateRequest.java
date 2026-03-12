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
    
    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;
    
    @Size(max = 50, message = "存储格式长度不能超过50")
    private String storageFormat;
    
    @Size(max = 500, message = "存储位置长度不能超过500")
    private String storageLocation;
    
    @Min(value = 0, message = "数据大小不能为负数")
    private Long dataSizeBytes;
}
