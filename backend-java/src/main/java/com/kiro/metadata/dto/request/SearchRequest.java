package com.kiro.metadata.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 搜索请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
    
    private Map<String, Object> filters;
    
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 20;
    
    private String sortBy = "relevance"; // relevance, updated_at, table_name
    
    private String sortOrder = "desc"; // asc, desc
}
