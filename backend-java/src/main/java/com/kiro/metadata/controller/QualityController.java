package com.kiro.metadata.controller;

import com.kiro.metadata.dto.response.QualityMetricsResponse;
import com.kiro.metadata.entity.QualityMetrics;
import com.kiro.metadata.service.QualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据质量控制器
 */
@RestController
@RequestMapping("/api/v1/quality")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "数据质量", description = "数据质量管理接口")
public class QualityController {

    private final QualityService qualityService;

    @PostMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "记录质量指标", description = "记录表的数据质量指标")
    @ApiResponse(responseCode = "200", description = "记录成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<QualityMetricsResponse> recordMetrics(@RequestBody Map<String, Object> request) {
        String tableId = (String) request.get("table_id");
        Long recordCount = ((Number) request.get("record_count")).longValue();
        Double nullRate = ((Number) request.get("null_rate")).doubleValue();
        String updateFrequency = (String) request.get("update_frequency");
        Integer dataFreshnessHours = ((Number) request.get("data_freshness_hours")).intValue();
        
        log.info("Record quality metrics request: tableId={}", tableId);
        QualityMetrics metrics = qualityService.recordQualityMetrics(
            tableId, recordCount, nullRate, updateFrequency, dataFreshnessHours);
        return ResponseEntity.ok(QualityMetricsResponse.from(metrics));
    }

    @GetMapping("/metrics/{tableId}")
    @Operation(summary = "获取质量指标", description = "获取表的最新质量指标")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "404", description = "质量指标不存在")
    public ResponseEntity<QualityMetricsResponse> getMetrics(@PathVariable String tableId) {
        log.info("Get quality metrics request: tableId={}", tableId);
        QualityMetrics metrics = qualityService.getQualityMetrics(tableId);
        return ResponseEntity.ok(QualityMetricsResponse.from(metrics));
    }

    @GetMapping("/trend/{tableId}")
    @Operation(summary = "获取质量趋势", description = "获取表的质量指标趋势")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<List<QualityMetricsResponse>> getTrend(
            @PathVariable String tableId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("Get quality trend request: tableId={}, days={}", tableId, days);
        List<QualityMetrics> trend = qualityService.getQualityTrend(tableId, days);
        List<QualityMetricsResponse> response = trend.stream()
            .map(QualityMetricsResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }
}
