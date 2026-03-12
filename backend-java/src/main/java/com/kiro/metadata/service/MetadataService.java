package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.TableCreateRequest;
import com.kiro.metadata.dto.request.TableUpdateRequest;
import com.kiro.metadata.dto.response.TableResponse;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.exception.DuplicateResourceException;
import com.kiro.metadata.exception.ResourceNotFoundException;
import com.kiro.metadata.repository.TableRepository;
import com.kiro.metadata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 元数据服务 - 表元数据 CRUD 操作
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataService {

    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final HistoryService historyService;
    private final SearchService searchService;

    /**
     * 创建表元数据
     */
    @Transactional
    @CacheEvict(value = "tables", allEntries = true)
    public TableMetadata createTable(TableCreateRequest request, String username) {
        log.info("Creating table: database={}, table={}, user={}", 
                 request.getDatabaseName(), request.getTableName(), username);

        // 检查表是否已存在
        if (tableRepository.existsByDatabaseNameAndTableName(
                request.getDatabaseName(), request.getTableName())) {
            throw new DuplicateResourceException(
                "表已存在: " + request.getDatabaseName() + "." + request.getTableName());
        }

        // 获取用户
        User owner = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        // 创建表元数据
        TableMetadata table = new TableMetadata();
        table.setDatabaseName(request.getDatabaseName());
        table.setTableName(request.getTableName());
        table.setTableType(request.getTableType());
        table.setDescription(request.getDescription());
        table.setStorageFormat(request.getStorageFormat());
        table.setStorageLocation(request.getStorageLocation());
        table.setDataSizeBytes(request.getDataSizeBytes());
        table.setOwner(owner);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());

        TableMetadata saved = tableRepository.save(table);
        log.info("Table created successfully: id={}", saved.getId());

        // 记录变更历史
        historyService.recordChange("TABLE", saved.getId(), "CREATE", null, null, null, username);

        // 同步到 Elasticsearch
        try {
            searchService.indexTable(saved);
        } catch (Exception e) {
            log.error("Failed to index table to Elasticsearch", e);
            // 不影响主流程
        }

        return saved;
    }

    /**
     * 根据 ID 获取表元数据
     */
    @Cacheable(value = "tables", key = "#tableId")
    public TableMetadata getTableById(String tableId) {
        log.debug("Getting table by id: {}", tableId);
        return tableRepository.findById(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("表不存在: " + tableId));
    }

    /**
     * 根据数据库名和表名获取表元数据
     */
    @Cacheable(value = "tables", key = "#databaseName + '_' + #tableName")
    public TableMetadata getTableByName(String databaseName, String tableName) {
        log.debug("Getting table by name: {}.{}", databaseName, tableName);
        return tableRepository.findByDatabaseNameAndTableName(databaseName, tableName)
            .orElseThrow(() -> new ResourceNotFoundException(
                "表不存在: " + databaseName + "." + tableName));
    }

    /**
     * 查询表列表（分页）
     */
    public Page<TableMetadata> listTables(Pageable pageable) {
        log.debug("Listing tables: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return tableRepository.findAll(pageable);
    }

    /**
     * 根据数据库名查询表列表
     */
    public Page<TableMetadata> listTablesByDatabase(String databaseName, Pageable pageable) {
        log.debug("Listing tables by database: database={}", databaseName);
        return tableRepository.findByDatabaseName(databaseName, pageable);
    }

    /**
     * 更新表元数据
     */
    @Transactional
    @CacheEvict(value = "tables", key = "#tableId")
    public TableMetadata updateTable(String tableId, TableUpdateRequest request, String username) {
        log.info("Updating table: id={}, user={}", tableId, username);

        TableMetadata table = getTableById(tableId);
        String oldValue = table.getDescription();

        // 更新字段
        if (request.getDescription() != null) {
            table.setDescription(request.getDescription());
        }
        if (request.getStorageFormat() != null) {
            table.setStorageFormat(request.getStorageFormat());
        }
        if (request.getStorageLocation() != null) {
            table.setStorageLocation(request.getStorageLocation());
        }
        if (request.getDataSizeBytes() != null) {
            table.setDataSizeBytes(request.getDataSizeBytes());
        }
        table.setUpdatedAt(LocalDateTime.now());

        TableMetadata updated = tableRepository.save(table);
        log.info("Table updated successfully: id={}", tableId);

        // 记录变更历史
        historyService.recordChange("TABLE", tableId, "UPDATE", 
                                   "description", oldValue, request.getDescription(), username);

        // 同步到 Elasticsearch
        try {
            searchService.indexTable(updated);
        } catch (Exception e) {
            log.error("Failed to update table index in Elasticsearch", e);
        }

        return updated;
    }

    /**
     * 删除表元数据
     */
    @Transactional
    @CacheEvict(value = "tables", key = "#tableId")
    public void deleteTable(String tableId, String username) {
        log.info("Deleting table: id={}, user={}", tableId, username);

        TableMetadata table = getTableById(tableId);

        // 记录变更历史
        historyService.recordChange("TABLE", tableId, "DELETE", null, 
                                   table.getDatabaseName() + "." + table.getTableName(), 
                                   null, username);

        // 删除表
        tableRepository.delete(table);
        log.info("Table deleted successfully: id={}", tableId);

        // 从 Elasticsearch 删除
        try {
            searchService.deleteFromIndex(tableId);
        } catch (Exception e) {
            log.error("Failed to delete table from Elasticsearch", e);
        }
    }

    /**
     * 更新表的最后访问时间
     */
    @Transactional
    public void updateLastAccessedAt(String tableId) {
        TableMetadata table = getTableById(tableId);
        table.setLastAccessedAt(LocalDateTime.now());
        tableRepository.save(table);
    }
}
