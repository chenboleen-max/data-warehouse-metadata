package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.CatalogCreateRequest;
import com.kiro.metadata.entity.Catalog;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.exception.MaxLevelExceededException;
import com.kiro.metadata.exception.ResourceNotFoundException;
import com.kiro.metadata.repository.CatalogRepository;
import com.kiro.metadata.repository.TableRepository;
import com.kiro.metadata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据目录服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final HistoryService historyService;

    private static final int MAX_LEVEL = 5;

    /**
     * 创建目录
     */
    @Transactional
    public Catalog createCatalog(CatalogCreateRequest request, String username) {
        log.info("Creating catalog: name={}, level={}, user={}", 
                 request.getName(), request.getLevel(), username);

        // 验证层级限制
        if (request.getLevel() > MAX_LEVEL) {
            throw new MaxLevelExceededException(request.getLevel(), MAX_LEVEL);
        }

        // 获取用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        // 验证父目录
        Catalog parent = null;
        if (request.getParentId() != null) {
            parent = catalogRepository.findById(request.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("父目录不存在: " + request.getParentId()));

            // 验证层级一致性
            if (request.getLevel() != parent.getLevel() + 1) {
                throw new IllegalArgumentException("目录层级必须是父目录层级+1");
            }
        }

        // 创建目录
        Catalog catalog = new Catalog();
        catalog.setName(request.getName());
        catalog.setDescription(request.getDescription());
        catalog.setParent(parent);
        catalog.setLevel(request.getLevel());
        catalog.setPath(buildPath(parent, request.getName()));
        catalog.setCreatedBy(user);
        catalog.setCreatedAt(LocalDateTime.now());
        catalog.setUpdatedAt(LocalDateTime.now());

        Catalog saved = catalogRepository.save(catalog);
        log.info("Catalog created successfully: id={}", saved.getId());

        // 记录变更历史
        historyService.recordChange("CATALOG", saved.getId(), "CREATE", null, null, null, username);

        return saved;
    }

    /**
     * 获取目录树
     */
    public List<Catalog> getCatalogTree() {
        log.debug("Getting catalog tree");
        return catalogRepository.findByParentIsNull();
    }

    /**
     * 移动目录
     */
    @Transactional
    public Catalog moveCatalog(String catalogId, String newParentId, String username) {
        log.info("Moving catalog: id={}, newParent={}, user={}", catalogId, newParentId, username);

        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResourceNotFoundException("目录不存在: " + catalogId));

        Catalog newParent = catalogRepository.findById(newParentId)
            .orElseThrow(() -> new ResourceNotFoundException("父目录不存在: " + newParentId));

        // 验证层级限制
        int newLevel = newParent.getLevel() + 1;
        if (newLevel > MAX_LEVEL) {
            throw new MaxLevelExceededException(newLevel, MAX_LEVEL);
        }

        // 更新目录
        catalog.setParent(newParent);
        catalog.setLevel(newLevel);
        catalog.setPath(buildPath(newParent, catalog.getName()));
        catalog.setUpdatedAt(LocalDateTime.now());

        Catalog updated = catalogRepository.save(catalog);
        log.info("Catalog moved successfully: id={}", catalogId);

        // 记录变更历史
        historyService.recordChange("CATALOG", catalogId, "UPDATE", 
                                   "parent_id", null, newParentId, username);

        return updated;
    }

    /**
     * 将表添加到目录
     */
    @Transactional
    public void addTableToCatalog(String catalogId, String tableId, String username) {
        log.info("Adding table to catalog: catalogId={}, tableId={}, user={}", 
                 catalogId, tableId, username);

        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResourceNotFoundException("目录不存在: " + catalogId));

        TableMetadata table = tableRepository.findById(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("表不存在: " + tableId));

        // 添加关联
        catalog.getTables().add(table);
        catalogRepository.save(catalog);

        log.info("Table added to catalog successfully");

        // 记录变更历史
        historyService.recordChange("CATALOG", catalogId, "UPDATE", 
                                   "add_table", null, tableId, username);
    }

    /**
     * 从目录中移除表
     */
    @Transactional
    public void removeTableFromCatalog(String catalogId, String tableId, String username) {
        log.info("Removing table from catalog: catalogId={}, tableId={}, user={}", 
                 catalogId, tableId, username);

        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResourceNotFoundException("目录不存在: " + catalogId));

        TableMetadata table = tableRepository.findById(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("表不存在: " + tableId));

        // 移除关联
        catalog.getTables().remove(table);
        catalogRepository.save(catalog);

        log.info("Table removed from catalog successfully");

        // 记录变更历史
        historyService.recordChange("CATALOG", catalogId, "UPDATE", 
                                   "remove_table", tableId, null, username);
    }

    /**
     * 获取目录下的表列表
     */
    public List<TableMetadata> getTablesInCatalog(String catalogId) {
        log.debug("Getting tables in catalog: {}", catalogId);

        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResourceNotFoundException("目录不存在: " + catalogId));

        return new ArrayList<>(catalog.getTables());
    }

    /**
     * 构建目录路径
     */
    private String buildPath(Catalog parent, String name) {
        if (parent == null) {
            return "/" + name;
        }
        return parent.getPath() + "/" + name;
    }
}
