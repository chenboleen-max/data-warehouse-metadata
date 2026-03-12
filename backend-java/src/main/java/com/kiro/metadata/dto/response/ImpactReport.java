package com.kiro.metadata.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 影响分析报告 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactReport {
    
    private String tableId;
    private String tableName;
    private List<String> affectedTableIds;
    private List<String> affectedTableNames;
    private Integer maxDepth;
    private Integer totalCount;
}

