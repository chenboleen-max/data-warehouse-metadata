package com.kiro.metadata.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    
    private List<TableResponse> results;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private String keyword;
    
    /**
     * 创建搜索响应
     */
    public static SearchResponse of(List<TableResponse> results, Long total, Integer page, Integer pageSize, String keyword) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        return SearchResponse.builder()
                .results(results)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .keyword(keyword)
                .build();
    }
}

