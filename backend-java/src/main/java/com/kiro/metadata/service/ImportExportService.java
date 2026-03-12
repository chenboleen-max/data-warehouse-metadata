package com.kiro.metadata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiro.metadata.dto.request.ImportRequest;
import com.kiro.metadata.dto.response.ExportStatusResponse;
import com.kiro.metadata.dto.response.ImportResultResponse;
import com.kiro.metadata.entity.ExportTask;
import com.kiro.metadata.entity.ExportType;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.entity.TaskStatus;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.exception.ResourceNotFoundException;
import com.kiro.metadata.repository.ExportTaskRepository;
import com.kiro.metadata.repository.TableRepository;
import com.kiro.metadata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 导入导出服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImportExportService {

    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final ExportTaskRepository exportTaskRepository;
    private final ObjectMapper objectMapper;

    private static final String EXPORT_DIR = "/tmp/exports";

    /**
     * 从 CSV 导入表元数据
     */
    @Transactional
    public ImportResultResponse importFromCsv(MultipartFile file, String username) {
        log.info("Importing tables from CSV: filename={}, user={}", file.getOriginalFilename(), username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        int successCount = 0;
        int failureCount = 0;
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (isHeader) {
                    isHeader = false;
                    continue; // 跳过表头
                }

                try {
                    String[] fields = line.split(",");
                    if (fields.length < 3) {
                        errors.add(ImportResultResponse.ImportError.builder()
                            .rowNumber(lineNumber)
                            .errorMessage("字段数量不足")
                            .build());
                        failureCount++;
                        continue;
                    }

                    // 解析字段
                    String databaseName = fields[0].trim();
                    String tableName = fields[1].trim();
                    String tableType = fields[2].trim();
                    String description = fields.length > 3 ? fields[3].trim() : null;

                    // 检查是否已存在
                    if (tableRepository.existsByDatabaseNameAndTableName(databaseName, tableName)) {
                        errors.add(ImportResultResponse.ImportError.builder()
                            .rowNumber(lineNumber)
                            .errorMessage("表已存在: " + databaseName + "." + tableName)
                            .build());
                        failureCount++;
                        continue;
                    }

                    // 创建表元数据
                    TableMetadata table = new TableMetadata();
                    table.setDatabaseName(databaseName);
                    table.setTableName(tableName);
                    table.setTableType(com.kiro.metadata.entity.TableType.valueOf(tableType.toUpperCase()));
                    table.setDescription(description);
                    table.setOwner(user);
                    table.setCreatedAt(LocalDateTime.now());
                    table.setUpdatedAt(LocalDateTime.now());

                    tableRepository.save(table);
                    successCount++;

                } catch (Exception e) {
                    log.error("Failed to import line {}", lineNumber, e);
                    errors.add(ImportResultResponse.ImportError.builder()
                        .rowNumber(lineNumber)
                        .errorMessage(e.getMessage())
                        .build());
                    failureCount++;
                }
            }

        } catch (IOException e) {
            log.error("Failed to read CSV file", e);
            throw new RuntimeException("读取 CSV 文件失败: " + e.getMessage());
        }

        log.info("CSV import completed: success={}, failure={}", successCount, failureCount);

        return ImportResultResponse.builder()
            .successCount(successCount)
            .failureCount(failureCount)
            .errors(errors)
            .build();
    }

    /**
     * 从 JSON 导入表元数据
     */
    @Transactional
    public ImportResultResponse importFromJson(MultipartFile file, String username) {
        log.info("Importing tables from JSON: filename={}, user={}", file.getOriginalFilename(), username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        int successCount = 0;
        int failureCount = 0;
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();

        try {
            // 解析 JSON 数组
            List<Map<String, Object>> tables = objectMapper.readValue(
                file.getInputStream(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            for (int i = 0; i < tables.size(); i++) {
                try {
                    Map<String, Object> tableData = tables.get(i);
                    
                    String databaseName = (String) tableData.get("database_name");
                    String tableName = (String) tableData.get("table_name");
                    String tableType = (String) tableData.get("table_type");
                    String description = (String) tableData.get("description");

                    // 检查是否已存在
                    if (tableRepository.existsByDatabaseNameAndTableName(databaseName, tableName)) {
                        errors.add(ImportResultResponse.ImportError.builder()
                            .rowNumber(i + 1)
                            .errorMessage("表已存在: " + databaseName + "." + tableName)
                            .build());
                        failureCount++;
                        continue;
                    }

                    // 创建表元数据
                    TableMetadata table = new TableMetadata();
                    table.setDatabaseName(databaseName);
                    table.setTableName(tableName);
                    table.setTableType(com.kiro.metadata.entity.TableType.valueOf(tableType.toUpperCase()));
                    table.setDescription(description);
                    table.setOwner(user);
                    table.setCreatedAt(LocalDateTime.now());
                    table.setUpdatedAt(LocalDateTime.now());

                    tableRepository.save(table);
                    successCount++;

                } catch (Exception e) {
                    log.error("Failed to import table at index {}", i, e);
                    errors.add(ImportResultResponse.ImportError.builder()
                        .rowNumber(i + 1)
                        .errorMessage(e.getMessage())
                        .build());
                    failureCount++;
                }
            }

        } catch (IOException e) {
            log.error("Failed to read JSON file", e);
            throw new RuntimeException("读取 JSON 文件失败: " + e.getMessage());
        }

        log.info("JSON import completed: success={}, failure={}", successCount, failureCount);

        return ImportResultResponse.builder()
            .successCount(successCount)
            .failureCount(failureCount)
            .errors(errors)
            .build();
    }

    /**
     * 导出为 CSV（异步）
     */
    @Async
    @Transactional
    public CompletableFuture<String> exportToCsv(Map<String, Object> filters, String username) {
        log.info("Exporting tables to CSV: user={}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        // 创建导出任务
        ExportTask task = new ExportTask();
        task.setTaskType(ExportType.CSV);
        task.setFilters(filters.toString());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());
        task = exportTaskRepository.save(task);

        String taskId = task.getId();

        try {
            // 更新状态为运行中
            task.setStatus(TaskStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());
            exportTaskRepository.save(task);

            // 查询数据
            List<TableMetadata> tables = tableRepository.findAll();

            // 创建导出目录
            Files.createDirectories(Paths.get(EXPORT_DIR));

            // 生成文件路径
            String filename = "tables_" + System.currentTimeMillis() + ".csv";
            Path filePath = Paths.get(EXPORT_DIR, filename);

            // 写入 CSV
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                // 写入表头
                writer.write("database_name,table_name,table_type,description,storage_format,data_size_bytes\n");

                // 写入数据
                for (TableMetadata table : tables) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%d\n",
                        table.getDatabaseName(),
                        table.getTableName(),
                        table.getTableType(),
                        table.getDescription() != null ? table.getDescription() : "",
                        table.getStorageFormat() != null ? table.getStorageFormat() : "",
                        table.getDataSizeBytes() != null ? table.getDataSizeBytes() : 0
                    ));
                }
            }

            // 更新任务状态
            task.setStatus(TaskStatus.COMPLETED);
            task.setFilePath(filePath.toString());
            task.setRecordCount(tables.size());
            task.setCompletedAt(LocalDateTime.now());
            exportTaskRepository.save(task);

            log.info("CSV export completed: taskId={}, records={}", taskId, tables.size());

        } catch (Exception e) {
            log.error("CSV export failed: taskId={}", taskId, e);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            exportTaskRepository.save(task);
        }

        return CompletableFuture.completedFuture(taskId);
    }

    /**
     * 导出为 JSON（异步）
     */
    @Async
    @Transactional
    public CompletableFuture<String> exportToJson(Map<String, Object> filters, String username) {
        log.info("Exporting tables to JSON: user={}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        // 创建导出任务
        ExportTask task = new ExportTask();
        task.setTaskType(ExportType.JSON);
        task.setFilters(filters.toString());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());
        task = exportTaskRepository.save(task);

        String taskId = task.getId();

        try {
            // 更新状态为运行中
            task.setStatus(TaskStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());
            exportTaskRepository.save(task);

            // 查询数据
            List<TableMetadata> tables = tableRepository.findAll();

            // 创建导出目录
            Files.createDirectories(Paths.get(EXPORT_DIR));

            // 生成文件路径
            String filename = "tables_" + System.currentTimeMillis() + ".json";
            Path filePath = Paths.get(EXPORT_DIR, filename);

            // 写入 JSON
            objectMapper.writeValue(filePath.toFile(), tables);

            // 更新任务状态
            task.setStatus(TaskStatus.COMPLETED);
            task.setFilePath(filePath.toString());
            task.setRecordCount(tables.size());
            task.setCompletedAt(LocalDateTime.now());
            exportTaskRepository.save(task);

            log.info("JSON export completed: taskId={}, records={}", taskId, tables.size());

        } catch (Exception e) {
            log.error("JSON export failed: taskId={}", taskId, e);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            exportTaskRepository.save(task);
        }

        return CompletableFuture.completedFuture(taskId);
    }

    /**
     * 获取导出任务状态
     */
    public ExportStatusResponse getExportStatus(String taskId) {
        log.debug("Getting export status: taskId={}", taskId);

        ExportTask task = exportTaskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("导出任务不存在: " + taskId));

        return ExportStatusResponse.builder()
            .id(task.getId())
            .status(task.getStatus())
            .taskType(task.getTaskType())
            .recordCount(task.getRecordCount())
            .filePath(task.getFilePath())
            .errorMessage(task.getErrorMessage())
            .createdAt(task.getCreatedAt())
            .startedAt(task.getStartedAt())
            .completedAt(task.getCompletedAt())
            .build();
    }

    /**
     * 下载导出文件
     */
    public File downloadExportFile(String taskId) {
        log.info("Downloading export file: taskId={}", taskId);

        ExportTask task = exportTaskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("导出任务不存在: " + taskId));

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("导出任务未完成");
        }

        File file = new File(task.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("导出文件不存在");
        }

        return file;
    }
}
