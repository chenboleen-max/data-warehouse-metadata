package com.kiro.metadata.dto.request;

import com.kiro.metadata.entity.ExportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 导出请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    
    @NotNull(message = "导出类型不能为空")
    private ExportType exportType; // CSV, JSON
    
    private Map<String, Object> filters; // 过滤条件
    
    private Boolean includeColumns = true; // 是否包含字段信息
}
