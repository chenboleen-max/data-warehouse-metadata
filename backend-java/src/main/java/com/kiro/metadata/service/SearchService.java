package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.SearchRequest;
import com.kiro.metadata.dto.response.SearchResponse;
import com.kiro.metadata.dto.response.TableResponse;
import com.kiro.metadata.dto.response.UserResponse;
import com.kiro.metadata.entity.TableMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务 - Elasticsearch 集成
 * 简化实现：使用内存搜索，生产环境应使用 Elasticsearch
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    // TODO: 集成 Elasticsearch
    // private final ElasticsearchRestTemplate elasticsearchTemplate;
    
    // 临时：使用内存存储索引
    private final Map<String, TableMetadata> indexedTables = new HashMap<>();

    /**
     * 创建索引
     */
    public void createIndex() {
        log.info("Creating Elasticsearch index");
        // TODO: 实现 Elasticsearch 索引创建
        log.warn("Elasticsearch not implemented, using in-memory search");
    }

    /**
     * 索引表元数据
     */
    public void indexTable(TableMetadata table) {
        log.debug("Indexing table: id={}", table.getId());
        // TODO: 实现 Elasticsearch 索引
        indexedTables.put(table.getId(), table);
    }

    /**
     * 批量索引表元数据
     */
    public void bulkIndexTables(List<TableMetadata> tables) {
        log.info("Bulk indexing {} tables", tables.size());
        // TODO: 实现 Elasticsearch 批量索引
        for (TableMetadata table : tables) {
            indexedTables.put(table.getId(), table);
        }
    }

    /**
     * 从索引中删除
     */
    public void deleteFromIndex(String tableId) {
        log.debug("Deleting from index: tableId={}", tableId);
        // TODO: 实现 Elasticsearch 删除
        indexedTables.remove(tableId);
    }

    /**
     * 全文搜索
     */
    public Page<TableResponse> searchTables(String keyword, Pageable pageable) {
        log.info("Searching tables: keyword={}", keyword);

        // TODO: 实现 Elasticsearch 全文搜索
        // 临时实现：内存搜索
        List<TableResponse> results = indexedTables.values().stream()
            .filter(table -> matchesKeyword(table, keyword))
            .map(this::toTableResponse)
            .collect(Collectors.toList());

        // 分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<TableResponse> pageContent = results.subList(start, end);

        return new PageImpl<>(pageContent, pageable, results.size());
    }

    /**
     * 搜索建议（自动补全）
     */
    public List<String> suggest(String prefix, int limit) {
        log.debug("Getting search suggestions: prefix={}, limit={}", prefix, limit);

        // TODO: 实现 Elasticsearch 自动补全
        // 临时实现：内存搜索
        return indexedTables.values().stream()
            .map(table -> table.getDatabaseName() + "." + table.getTableName())
            .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 高级过滤搜索
     */
    public Page<TableResponse> filterTables(SearchRequest request, Pageable pageable) {
        log.info("Filtering tables: request={}", request);

        // TODO: 实现 Elasticsearch 高级过滤
        // 临时实现：内存过滤
        List<TableResponse> results = indexedTables.values().stream()
            .filter(table -> matchesFilters(table, request))
            .map(this::toTableResponse)
            .collect(Collectors.toList());

        // 分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<TableResponse> pageContent = results.subList(start, end);

        return new PageImpl<>(pageContent, pageable, results.size());
    }

    /**
     * 检查表是否匹配关键词
     */
    private boolean matchesKeyword(TableMetadata table, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }

        String lowerKeyword = keyword.toLowerCase();
        
        // 搜索表名
        if (table.getTableName().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // 搜索数据库名
        if (table.getDatabaseName().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // 搜索描述
        if (table.getDescription() != null && 
            table.getDescription().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        return false;
    }

    /**
     * 检查表是否匹配过滤条件
     */
    private boolean matchesFilters(TableMetadata table, SearchRequest request) {
        if (request.getFilters() == null || request.getFilters().isEmpty()) {
            return true;
        }

        // 数据库名过滤
        if (request.getFilters().containsKey("databaseName")) {
            String databaseName = (String) request.getFilters().get("databaseName");
            if (databaseName != null && !table.getDatabaseName().equals(databaseName)) {
                return false;
            }
        }

        // 表类型过滤
        if (request.getFilters().containsKey("tableType")) {
            String tableType = (String) request.getFilters().get("tableType");
            if (tableType != null && !table.getTableType().equals(tableType)) {
                return false;
            }
        }

        // 关键词过滤
        if (request.getKeyword() != null && 
            !matchesKeyword(table, request.getKeyword())) {
            return false;
        }

        return true;
    }

    /**
     * 转换为表响应
     */
    private TableResponse toTableResponse(TableMetadata table) {
        return TableResponse.builder()
            .id(table.getId())
            .databaseName(table.getDatabaseName())
            .tableName(table.getTableName())
            .tableType(table.getTableType())
            .description(table.getDescription())
            .storageFormat(table.getStorageFormat())
            .dataSizeBytes(table.getDataSizeBytes())
            .createdAt(table.getCreatedAt())
            .updatedAt(table.getUpdatedAt())
            .owner(table.getOwner() != null ? UserResponse.from(table.getOwner()) : null)
            .build();
    }
}
