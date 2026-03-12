package com.kiro.metadata.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据目录创建请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogCreateRequest {
    
    @NotBlank(message = "目录名称不能为空")
    @Size(min = 1, max = 100, message = "目录名称长度必须在1-100之间")
    private String name;
    
    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;
    
    private String parentId;
    
    @NotNull(message = "层级不能为空")
    @Min(value = 1, message = "层级必须大于等于1")
    @Max(value = 5, message = "层级不能超过5")
    private Integer level;
}
