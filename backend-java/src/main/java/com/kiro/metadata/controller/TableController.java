package com.kiro.metadata.controller;

import com.kiro.metadata.dto.request.TableCreateRequest;
import com.kiro.metadata.dto.request.TableUpdateRequest;
import com.kiro.metadata.dto.response.ColumnResponse;
import com.kiro.metadata.dto.response.PagedResponse;
import com.kiro.metadata.dto.response.TableResponse;
import com.kiro.metadata.entity.ColumnMetadata;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.service.ColumnService;
import com.kiro.metadata.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表元数据控制器
 */
@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "表元数据", description = "表元数据管理接口")
public class TableController {

    private final MetadataService metadataService;
    private final ColumnService columnService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "创建表", description = "创建新的表元数据")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @ApiResponse(responseCode = "400", description = "请求参数无效")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "409", description = "表已存在")
    public ResponseEntity<TableResponse> createTable(
            @Valid @RequestBody TableCreateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Create table request: user={}", username);
        TableMetadata table = metadataService.createTable(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(TableResponse.from(table));
    }

    @GetMapping
    @Operation(summary = "获取表列表", description = "分页查询表列表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<PagedResponse<TableResponse>> listTables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("List tables request: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TableMetadata> tablePage = metadataService.listTables(pageable);
        
        List<TableResponse> content = tablePage.getContent().stream()
            .map(TableResponse::from)
            .toList();
        
        PagedResponse<TableResponse> response = PagedResponse.<TableResponse>builder()
            .items(content)
            .page(tablePage.getNumber())
            .pageSize(tablePage.getSize())
            .total(tablePage.getTotalElements())
            .totalPages(tablePage.getTotalPages())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取表详情", description = "根据 ID 获取表的详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "404", description = "表不存在")
    public ResponseEntity<TableResponse> getTable(@PathVariable String id) {
        log.info("Get table request: id={}", id);
        TableMetadata table = metadataService.getTableById(id);
        return ResponseEntity.ok(TableResponse.from(table));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "更新表", description = "更新表元数据")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "404", description = "表不存在")
    public ResponseEntity<TableResponse> updateTable(
            @PathVariable String id,
            @Valid @RequestBody TableUpdateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Update table request: id={}, user={}", id, username);
        TableMetadata table = metadataService.updateTable(id, request, username);
        return ResponseEntity.ok(TableResponse.from(table));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除表", description = "删除表元数据")
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "404", description = "表不存在")
    public ResponseEntity<Void> deleteTable(
            @PathVariable String id,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Delete table request: id={}, user={}", id, username);
        metadataService.deleteTable(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/columns")
    @Operation(summary = "获取表的字段列表", description = "获取指定表的所有字段")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "404", description = "表不存在")
    public ResponseEntity<List<ColumnResponse>> getTableColumns(@PathVariable String id) {
        log.info("Get table columns request: tableId={}", id);
        List<ColumnMetadata> columns = columnService.getColumnsByTableId(id);
        List<ColumnResponse> response = columns.stream()
            .map(ColumnResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }
}
