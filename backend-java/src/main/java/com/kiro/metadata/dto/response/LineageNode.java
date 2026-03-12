package com.kiro.metadata.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * 血缘关系图节点 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageNode {
    
    private String id;
    private String name; // database.table
    private Integer depth;
    private String type; // root, upstream, downstream
}

