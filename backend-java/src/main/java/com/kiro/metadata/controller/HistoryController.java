package com.kiro.metadata.controller;

import com.kiro.metadata.dto.response.ChangeHistoryResponse;
import com.kiro.metadata.dto.response.PagedResponse;
import com.kiro.metadata.entity.ChangeHistory;
import com.kiro.metadata.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 变更历史控制器
 */
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "变更历史", description = "变更历史查询接口")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "获取实体变更历史", description = "获取指定实体的变更历史记录")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<PagedResponse<ChangeHistoryResponse>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get entity history request: entityType={}, entityId={}", entityType, entityId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ChangeHistory> historyPage = historyService.getEntityHistory(entityType, entityId, pageable);
        
        PagedResponse<ChangeHistoryResponse> response = PagedResponse.<ChangeHistoryResponse>builder()
            .items(historyPage.getContent().stream()
                .map(ChangeHistoryResponse::from)
                .toList())
            .page(historyPage.getNumber())
            .pageSize(historyPage.getSize())
            .total(historyPage.getTotalElements())
            .totalPages(historyPage.getTotalPages())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户操作记录", description = "获取指定用户的所有操作记录")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<PagedResponse<ChangeHistoryResponse>> getUserActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get user activity request: userId={}", userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ChangeHistory> historyPage = historyService.getUserActivity(userId, pageable);
        
        PagedResponse<ChangeHistoryResponse> response = PagedResponse.<ChangeHistoryResponse>builder()
            .items(historyPage.getContent().stream()
                .map(ChangeHistoryResponse::from)
                .toList())
            .page(historyPage.getNumber())
            .pageSize(historyPage.getSize())
            .total(historyPage.getTotalElements())
            .totalPages(historyPage.getTotalPages())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compare")
    @Operation(summary = "对比版本", description = "对比实体的两个版本")
    @ApiResponse(responseCode = "200", description = "对比成功")
    public ResponseEntity<Map<String, Object>> compareVersions(@RequestBody Map<String, String> request) {
        String entityId = request.get("entity_id");
        String version1 = request.get("version1");
        String version2 = request.get("version2");
        
        log.info("Compare versions request: entityId={}, v1={}, v2={}", entityId, version1, version2);
        Map<String, Object> diff = historyService.compareVersions(entityId, version1, version2);
        return ResponseEntity.ok(diff);
    }
}
