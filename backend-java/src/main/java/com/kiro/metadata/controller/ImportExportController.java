package com.kiro.metadata.controller;

import com.kiro.metadata.dto.response.ExportStatusResponse;
import com.kiro.metadata.dto.response.ImportResultResponse;
import com.kiro.metadata.service.ImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 导入导出控制器
 */
@RestController
@RequestMapping("/api/v1/import-export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "导入导出", description = "数据导入导出接口")
public class ImportExportController {

    private final ImportExportService importExportService;

    @PostMapping("/import/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "导入 CSV", description = "从 CSV 文件导入表元数据")
    @ApiResponse(responseCode = "200", description = "导入成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<ImportResultResponse> importCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Import CSV request: filename={}, user={}", file.getOriginalFilename(), username);
        ImportResultResponse result = importExportService.importFromCsv(file, username);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/json")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "导入 JSON", description = "从 JSON 文件导入表元数据")
    @ApiResponse(responseCode = "200", description = "导入成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<ImportResultResponse> importJson(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Import JSON request: filename={}, user={}", file.getOriginalFilename(), username);
        ImportResultResponse result = importExportService.importFromJson(file, username);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "导出数据", description = "异步导出表元数据")
    @ApiResponse(responseCode = "202", description = "导出任务已创建")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<Map<String, String>> exportData(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String username = authentication.getName();
        String format = (String) request.getOrDefault("format", "csv");
        Map<String, Object> filters = (Map<String, Object>) request.getOrDefault("filters", new HashMap<>());
        
        log.info("Export request: format={}, user={}", format, username);
        
        CompletableFuture<String> taskIdFuture;
        if ("json".equalsIgnoreCase(format)) {
            taskIdFuture = importExportService.exportToJson(filters, username);
        } else {
            taskIdFuture = importExportService.exportToCsv(filters, username);
        }
        
        // 等待任务创建完成
        String taskId = taskIdFuture.join();
        
        Map<String, String> response = new HashMap<>();
        response.put("task_id", taskId);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/export/{taskId}/status")
    @Operation(summary = "查询导出状态", description = "查询导出任务的执行状态")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<ExportStatusResponse> getExportStatus(@PathVariable String taskId) {
        log.info("Get export status request: taskId={}", taskId);
        ExportStatusResponse status = importExportService.getExportStatus(taskId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/export/{taskId}/download")
    @Operation(summary = "下载导出文件", description = "下载已完成的导出文件")
    @ApiResponse(responseCode = "200", description = "下载成功")
    @ApiResponse(responseCode = "404", description = "文件不存在")
    public ResponseEntity<Resource> downloadExportFile(@PathVariable String taskId) {
        log.info("Download export file request: taskId={}", taskId);
        File file = importExportService.downloadExportFile(taskId);
        
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
