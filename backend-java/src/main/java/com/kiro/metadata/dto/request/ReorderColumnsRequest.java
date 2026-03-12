package com.kiro.metadata.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 字段重新排序请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderColumnsRequest {
    
    @NotNull(message = "表ID不能为空")
    private String tableId;
    
    @NotEmpty(message = "字段ID列表不能为空")
    private List<String> columnIds; // 按新顺序排列的字段ID列表
}
