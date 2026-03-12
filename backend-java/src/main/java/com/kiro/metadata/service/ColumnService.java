package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.ColumnCreateRequest;
import com.kiro.metadata.dto.request.ColumnUpdateRequest;
import com.kiro.metadata.dto.request.ReorderColumnsRequest;
import com.kiro.metadata.entity.ColumnMetadata;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.exception.ResourceNotFoundException;
import com.kiro.metadata.repository.ColumnRepository;
import com.kiro.metadata.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字段元数据服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ColumnService {

    private final ColumnRepository columnRepository;
    private final TableRepository tableRepository;
    private final HistoryService historyService;

    /**
     * 创建字段元数据
     */
    @Transactional
    @CacheEvict(value = "columns", key = "#request.tableId")
    public ColumnMetadata createColumn(ColumnCreateRequest request, String username) {
        log.info("Creating column: table={}, column={}", request.getTableId(), request.getColumnName());

        // 获取表
        TableMetadata table = tableRepository.findById(request.getTableId())
            .orElseThrow(() -> new ResourceNotFoundException("表不存在: " + request.getTableId()));

        // 创建字段
        ColumnMetadata column = new ColumnMetadata();
        column.setTable(table);
        column.setColumnName(request.getColumnName());
        column.setDataType(request.getDataType());
        column.setColumnOrder(request.getColumnOrder());
        column.setIsNullable(request.getIsNullable() != null ? request.getIsNullable() : true);
        column.setIsPartitionKey(request.getIsPartitionKey() != null ? request.getIsPartitionKey() : false);
        column.setDescription(request.getDescription());
        column.setCreatedAt(LocalDateTime.now());
        column.setUpdatedAt(LocalDateTime.now());

        ColumnMetadata saved = columnRepository.save(column);
        log.info("Column created successfully: id={}", saved.getId());

        // 记录变更历史
        historyService.recordChange("COLUMN", saved.getId(), "CREATE", null, null, null, username);

        return saved;
    }

    /**
     * 根据表 ID 获取字段列表
     */
    @Cacheable(value = "columns", key = "#tableId")
    public List<ColumnMetadata> getColumnsByTableId(String tableId) {
        log.debug("Getting columns by table id: {}", tableId);
        return columnRepository.findByTableIdOrderByColumnOrder(tableId);
    }

    /**
     * 更新字段元数据
     */
    @Transactional
    @CacheEvict(value = "columns", key = "#result.table.id")
    public ColumnMetadata updateColumn(String columnId, ColumnUpdateRequest request, String username) {
        log.info("Updating column: id={}", columnId);

        ColumnMetadata column = columnRepository.findById(columnId)
            .orElseThrow(() -> new ResourceNotFoundException("字段不存在: " + columnId));

        String oldDescription = column.getDescription();

        // 更新字段
        if (request.getDescription() != null) {
            column.setDescription(request.getDescription());
        }
        if (request.getDataType() != null) {
            column.setDataType(request.getDataType());
        }
        if (request.getIsNullable() != null) {
            column.setIsNullable(request.getIsNullable());
        }
        if (request.getIsPartitionKey() != null) {
            column.setIsPartitionKey(request.getIsPartitionKey());
        }
        column.setUpdatedAt(LocalDateTime.now());

        ColumnMetadata updated = columnRepository.save(column);
        log.info("Column updated successfully: id={}", columnId);

        // 记录变更历史
        historyService.recordChange("COLUMN", columnId, "UPDATE", 
                                   "description", oldDescription, request.getDescription(), username);

        return updated;
    }

    /**
     * 删除字段元数据
     */
    @Transactional
    @CacheEvict(value = "columns", key = "#result")
    public String deleteColumn(String columnId, String username) {
        log.info("Deleting column: id={}", columnId);

        ColumnMetadata column = columnRepository.findById(columnId)
            .orElseThrow(() -> new ResourceNotFoundException("字段不存在: " + columnId));

        String tableId = column.getTable().getId();

        // 记录变更历史
        historyService.recordChange("COLUMN", columnId, "DELETE", null, 
                                   column.getColumnName(), null, username);

        // 删除字段
        columnRepository.delete(column);
        log.info("Column deleted successfully: id={}", columnId);

        return tableId;
    }

    /**
     * 重新排序字段
     */
    @Transactional
    @CacheEvict(value = "columns", key = "#request.tableId")
    public void reorderColumns(ReorderColumnsRequest request, String username) {
        log.info("Reordering columns for table: {}", request.getTableId());

        List<String> columnIds = request.getColumnIds();
        for (int i = 0; i < columnIds.size(); i++) {
            String columnId = columnIds.get(i);
            ColumnMetadata column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("字段不存在: " + columnId));
            
            column.setColumnOrder(i + 1);
            column.setUpdatedAt(LocalDateTime.now());
            columnRepository.save(column);
        }

        log.info("Columns reordered successfully for table: {}", request.getTableId());

        // 记录变更历史
        historyService.recordChange("TABLE", request.getTableId(), "UPDATE", 
                                   "column_order", null, "reordered", username);
    }

    /**
     * 更新字段描述
     */
    @Transactional
    @CacheEvict(value = "columns", key = "#result.table.id")
    public ColumnMetadata updateColumnDescription(String columnId, String description, String username) {
        log.info("Updating column description: id={}", columnId);

        ColumnMetadata column = columnRepository.findById(columnId)
            .orElseThrow(() -> new ResourceNotFoundException("字段不存在: " + columnId));

        String oldDescription = column.getDescription();
        column.setDescription(description);
        column.setUpdatedAt(LocalDateTime.now());

        ColumnMetadata updated = columnRepository.save(column);
        log.info("Column description updated successfully: id={}", columnId);

        // 记录变更历史
        historyService.recordChange("COLUMN", columnId, "UPDATE", 
                                   "description", oldDescription, description, username);

        return updated;
    }
}
