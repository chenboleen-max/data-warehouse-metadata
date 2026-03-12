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
    
    @NotBlank(message = "数据库名称不能为空")
    @Size(min = 1, max = 100, message = "数据库名称长度必须在1-100之间")
    private String databaseName;
    
    @NotBlank(message = "表名不能为空")
    @Size(min = 1, max = 100, message = "表名长度必须在1-100之间")
    private String tableName;
    
    @NotNull(message = "表类型不能为空")
    private TableType tableType;
    
    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;
    
    @Size(max = 50, message = "存储格式长度不能超过50")
    private String storageFormat;
    
    @Size(max = 500, message = "存储位置长度不能超过500")
    private String storageLocation;
    
    @Min(value = 0, message = "数据大小不能为负数")
    private Long dataSizeBytes;
}
