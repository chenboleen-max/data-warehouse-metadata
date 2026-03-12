package com.kiro.metadata.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 导入请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequest {
    
    @NotNull(message = "文件类型不能为空")
    private String fileType; // CSV, JSON
    
    private Map<String, String> fieldMapping; // 字段映射
    
    private Boolean skipErrors = false; // 是否跳过错误行
    
    private Boolean dryRun = false; // 是否仅验证不导入
}
