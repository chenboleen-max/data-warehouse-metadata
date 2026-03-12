package com.kiro.metadata.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段更新请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnUpdateRequest {
    
    @Size(min = 1, max = 50, message = "数据类型长度必须在1-50之间")
    private String dataType;
    
    @Min(value = 1, message = "字段顺序必须大于0")
    private Integer columnOrder;
    
    private Boolean isNullable;
    
    private Boolean isPartitionKey;
    
    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;
}
