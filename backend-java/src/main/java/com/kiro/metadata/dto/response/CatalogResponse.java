package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.Catalog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

/**
 * 鏁版嵁鐩綍鍝嶅簲 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponse {
    
    private String id;
    private String name;
    private String description;
    private String parentId;
    private Integer level;
    private String path;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse createdBy;
    private List<CatalogResponse> children;
    
    /**
     * 浠?Catalog 瀹炰綋杞崲涓?CatalogResponse
     */
    public static CatalogResponse from(Catalog catalog) {
        return CatalogResponse.builder()
                .id(catalog.getId())
                .name(catalog.getName())
                .description(catalog.getDescription())
                .parentId(catalog.getParent() != null ? catalog.getParent().getId() : null)
                .level(catalog.getLevel())
                .path(catalog.getPath())
                .createdAt(catalog.getCreatedAt())
                .updatedAt(catalog.getUpdatedAt())
                .createdBy(catalog.getCreatedBy() != null ? UserResponse.from(catalog.getCreatedBy()) : null)
                .children(catalog.getChildren() != null ? 
                        catalog.getChildren().stream()
                                .map(CatalogResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}

