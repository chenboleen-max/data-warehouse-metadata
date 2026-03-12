package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.LineageCreateRequest;
import com.kiro.metadata.entity.LineageType;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 解析服务 - 从 SQL 提取血缘关系
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SqlParserService {

    private final TableRepository tableRepository;

    // 正则表达式模式
    private static final Pattern FROM_PATTERN = Pattern.compile(
        "FROM\\s+([\\w.]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOIN_PATTERN = Pattern.compile(
        "JOIN\\s+([\\w.]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INSERT_PATTERN = Pattern.compile(
        "INSERT\\s+INTO\\s+([\\w.]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_TABLE_AS_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+([\\w.]+)\\s+AS", Pattern.CASE_INSENSITIVE);

    /**
     * 从 SQL 提取血缘关系
     * 简单实现：使用正则表达式提取表名
     */
    public List<LineageCreateRequest> extractLineageFromSql(String sql) {
        log.info("Extracting lineage from SQL");

        List<LineageCreateRequest> lineages = new ArrayList<>();

        try {
            // 提取目标表（INSERT INTO 或 CREATE TABLE AS）
            String targetTableId = extractTargetTable(sql);
            if (targetTableId == null) {
                log.warn("No target table found in SQL");
                return lineages;
            }

            // 提取源表（FROM 和 JOIN）
            Set<String> sourceTableIds = extractSourceTables(sql);

            // 创建血缘关系
            for (String sourceTableId : sourceTableIds) {
                LineageCreateRequest lineage = new LineageCreateRequest();
                lineage.setSourceTableId(sourceTableId);
                lineage.setTargetTableId(targetTableId);
                lineage.setLineageType(LineageType.DIRECT);
                lineage.setTransformationLogic(sql);
                lineages.add(lineage);
            }

            log.info("Extracted {} lineage relationships from SQL", lineages.size());

        } catch (Exception e) {
            log.error("Failed to parse SQL", e);
        }

        return lineages;
    }

    /**
     * 提取目标表
     */
    private String extractTargetTable(String sql) {
        // 尝试 INSERT INTO
        Matcher insertMatcher = INSERT_PATTERN.matcher(sql);
        if (insertMatcher.find()) {
            String tableName = insertMatcher.group(1);
            return findTableIdByName(tableName);
        }

        // 尝试 CREATE TABLE AS
        Matcher createMatcher = CREATE_TABLE_AS_PATTERN.matcher(sql);
        if (createMatcher.find()) {
            String tableName = createMatcher.group(1);
            return findTableIdByName(tableName);
        }

        return null;
    }

    /**
     * 提取源表
     */
    private Set<String> extractSourceTables(String sql) {
        Set<String> sourceTableIds = new HashSet<>();

        // 提取 FROM 子句中的表
        Matcher fromMatcher = FROM_PATTERN.matcher(sql);
        while (fromMatcher.find()) {
            String tableName = fromMatcher.group(1);
            String tableId = findTableIdByName(tableName);
            if (tableId != null) {
                sourceTableIds.add(tableId);
            }
        }

        // 提取 JOIN 子句中的表
        Matcher joinMatcher = JOIN_PATTERN.matcher(sql);
        while (joinMatcher.find()) {
            String tableName = joinMatcher.group(1);
            String tableId = findTableIdByName(tableName);
            if (tableId != null) {
                sourceTableIds.add(tableId);
            }
        }

        return sourceTableIds;
    }

    /**
     * 根据表名查找表 ID
     */
    private String findTableIdByName(String fullTableName) {
        String[] parts = fullTableName.split("\\.");
        String databaseName;
        String tableName;

        if (parts.length == 2) {
            databaseName = parts[0];
            tableName = parts[1];
        } else {
            // 假设只有表名，使用默认数据库
            databaseName = "default";
            tableName = fullTableName;
        }

        Optional<TableMetadata> table = tableRepository.findByDatabaseNameAndTableName(
            databaseName, tableName);
        
        return table.map(TableMetadata::getId).orElse(null);
    }
}
