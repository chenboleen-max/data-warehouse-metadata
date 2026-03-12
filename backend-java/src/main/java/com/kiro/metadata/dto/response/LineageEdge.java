package com.kiro.metadata.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * è¡ç¼å³ç³»å¾è¾?DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageEdge {
    
    private String source;
    private String target;
    private String type; // DIRECT, INDIRECT
}

