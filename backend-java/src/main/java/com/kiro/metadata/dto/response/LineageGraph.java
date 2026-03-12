package com.kiro.metadata.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 血缘关系图 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageGraph {
    
    private List<LineageNode> nodes;
    private List<LineageEdge> edges;
}

