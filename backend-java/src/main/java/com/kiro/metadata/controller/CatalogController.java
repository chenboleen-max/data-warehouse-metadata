package com.kiro.metadata.controller;

import com.kiro.metadata.dto.request.CatalogCreateRequest;
import com.kiro.metadata.dto.response.CatalogResponse;
import com.kiro.metadata.dto.response.TableResponse;
import com.kiro.metadata.entity.Catalog;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.service.CatalogService;
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
 * 数据目录控制器
 */
@RestController
@RequestMapping("/api/v1/catalogs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "数据目录", description = "数据目录管理接口")
public class CatalogController {

    private final CatalogService catalogService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "创建目录", description = "创建新的数据目录节点")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @ApiResponse(responseCode = "400", description = "层级超过限制")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<CatalogResponse> createCatalog(
            @Valid @RequestBody CatalogCreateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Create catalog request: user={}", username);
        Catalog catalog = catalogService.createCatalog(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(CatalogResponse.from(catalog));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取目录树", description = "获取完整的目录树结构")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<List<CatalogResponse>> getCatalogTree() {
        log.info("Get catalog tree request");
        List<Catalog> catalogs = catalogService.getCatalogTree();
        List<CatalogResponse> response = catalogs.stream()
            .map(CatalogResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/move")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "移动目录", description = "将目录移动到新的父目录下")
    @ApiResponse(responseCode = "200", description = "移动成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<CatalogResponse> moveCatalog(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String username = authentication.getName();
        String newParentId = request.get("new_parent_id");
        log.info("Move catalog request: id={}, newParentId={}, user={}", id, newParentId, username);
        Catalog catalog = catalogService.moveCatalog(id, newParentId, username);
        return ResponseEntity.ok(CatalogResponse.from(catalog));
    }

    @PostMapping("/{id}/tables")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "添加表到目录", description = "将表关联到目录")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<Void> addTableToCatalog(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String username = authentication.getName();
        String tableId = request.get("table_id");
        log.info("Add table to catalog request: catalogId={}, tableId={}, user={}", 
                 id, tableId, username);
        catalogService.addTableToCatalog(id, tableId, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/tables/{tableId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "从目录移除表", description = "取消表与目录的关联")
    @ApiResponse(responseCode = "204", description = "移除成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<Void> removeTableFromCatalog(
            @PathVariable String id,
            @PathVariable String tableId,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Remove table from catalog request: catalogId={}, tableId={}, user={}", 
                 id, tableId, username);
        catalogService.removeTableFromCatalog(id, tableId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tables")
    @Operation(summary = "获取目录下的表", description = "获取指定目录下的所有表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<List<TableResponse>> getTablesInCatalog(@PathVariable String id) {
        log.info("Get tables in catalog request: catalogId={}", id);
        List<TableMetadata> tables = catalogService.getTablesInCatalog(id);
        List<TableResponse> response = tables.stream()
            .map(TableResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }
}
