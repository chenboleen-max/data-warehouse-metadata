package com.kiro.metadata.dto.request;

import com.kiro.metadata.entity.LineageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 血缘关系创建请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineageCreateRequest {
    
    @NotNull(message = "源表ID不能为空")
    private String sourceTableId;
    
    @NotNull(message = "目标表ID不能为空")
    private String targetTableId;
    
    @NotNull(message = "血缘类型不能为空")
    private LineageType lineageType;
    
    @Size(max = 5000, message = "转换逻辑长度不能超过5000")
    private String transformationLogic;
}
