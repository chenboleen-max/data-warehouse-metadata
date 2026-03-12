package com.kiro.metadata.controller;

import com.kiro.metadata.dto.request.LineageCreateRequest;
import com.kiro.metadata.dto.response.ImpactReport;
import com.kiro.metadata.dto.response.LineageGraph;
import com.kiro.metadata.dto.response.LineageResponse;
import com.kiro.metadata.entity.Lineage;
import com.kiro.metadata.service.LineageService;
import com.kiro.metadata.service.SqlParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 血缘关系控制器
 */
@RestController
@RequestMapping("/api/v1/lineage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "血缘关系", description = "血缘关系管理接口")
public class LineageController {

    private final LineageService lineageService;
    private final SqlParserService sqlParserService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "创建血缘关系", description = "创建表之间的血缘关系")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "409", description = "血缘关系已存在")
    public ResponseEntity<LineageResponse> createLineage(
            @Valid @RequestBody LineageCreateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Create lineage request: user={}", username);
        Lineage lineage = lineageService.createLineage(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(LineageResponse.from(lineage));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除血缘关系", description = "删除指定的血缘关系")
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "404", description = "血缘关系不存在")
    public ResponseEntity<Void> deleteLineage(
            @PathVariable String id,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Delete lineage request: id={}, user={}", id, username);
        lineageService.deleteLineage(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upstream/{tableId}")
    @Operation(summary = "获取上游表", description = "获取指定表的所有上游表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<List<LineageResponse>> getUpstreamTables(@PathVariable String tableId) {
        log.info("Get upstream tables request: tableId={}", tableId);
        var tables = lineageService.getUpstreamTables(tableId);
        // TODO: Convert to LineageResponse
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/downstream/{tableId}")
    @Operation(summary = "获取下游表", description = "获取指定表的所有下游表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<List<LineageResponse>> getDownstreamTables(@PathVariable String tableId) {
        log.info("Get downstream tables request: tableId={}", tableId);
        var tables = lineageService.getDownstreamTables(tableId);
        // TODO: Convert to LineageResponse
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/graph/{tableId}")
    @Operation(summary = "获取血缘关系图", description = "获取指定表的血缘关系图")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<LineageGraph> getLineageGraph(
            @PathVariable String tableId,
            @RequestParam(defaultValue = "both") String direction,
            @RequestParam(defaultValue = "3") int maxDepth) {
        log.info("Get lineage graph request: tableId={}, direction={}, maxDepth={}", 
                 tableId, direction, maxDepth);
        LineageGraph graph = lineageService.buildLineageGraph(tableId, direction, maxDepth);
        return ResponseEntity.ok(graph);
    }

    @PostMapping("/impact")
    @Operation(summary = "影响分析", description = "分析表变更的影响范围")
    @ApiResponse(responseCode = "200", description = "分析成功")
    public ResponseEntity<ImpactReport> analyzeImpact(@RequestBody Map<String, String> request) {
        String tableId = request.get("table_id");
        log.info("Analyze impact request: tableId={}", tableId);
        ImpactReport report = lineageService.analyzeImpact(tableId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/parse-sql")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "解析 SQL", description = "从 SQL 语句中提取血缘关系")
    @ApiResponse(responseCode = "200", description = "解析成功")
    public ResponseEntity<List<LineageCreateRequest>> parseSql(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        log.info("Parse SQL request");
        List<LineageCreateRequest> lineages = sqlParserService.extractLineageFromSql(sql);
        return ResponseEntity.ok(lineages);
    }
}
