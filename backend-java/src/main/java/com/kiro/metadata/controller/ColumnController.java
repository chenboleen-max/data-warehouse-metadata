package com.kiro.metadata.controller;

import com.kiro.metadata.dto.request.ColumnCreateRequest;
import com.kiro.metadata.dto.request.ColumnUpdateRequest;
import com.kiro.metadata.dto.request.ReorderColumnsRequest;
import com.kiro.metadata.dto.response.ColumnResponse;
import com.kiro.metadata.entity.ColumnMetadata;
import com.kiro.metadata.service.ColumnService;
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

/**
 * 字段元数据控制器
 */
@RestController
@RequestMapping("/api/v1/columns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "字段元数据", description = "字段元数据管理接口")
public class ColumnController {

    private final ColumnService columnService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "创建字段", description = "为表创建新的字段")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<ColumnResponse> createColumn(
            @Valid @RequestBody ColumnCreateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Create column request: user={}", username);
        ColumnMetadata column = columnService.createColumn(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(ColumnResponse.from(column));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "更新字段", description = "更新字段元数据")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "404", description = "字段不存在")
    public ResponseEntity<ColumnResponse> updateColumn(
            @PathVariable String id,
            @Valid @RequestBody ColumnUpdateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Update column request: id={}, user={}", id, username);
        ColumnMetadata column = columnService.updateColumn(id, request, username);
        return ResponseEntity.ok(ColumnResponse.from(column));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除字段", description = "删除字段元数据")
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "404", description = "字段不存在")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable String id,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Delete column request: id={}, user={}", id, username);
        columnService.deleteColumn(id, username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "重新排序字段", description = "调整表字段的显示顺序")
    @ApiResponse(responseCode = "200", description = "排序成功")
    @ApiResponse(responseCode = "403", description = "无权限")
    public ResponseEntity<Void> reorderColumns(
            @Valid @RequestBody ReorderColumnsRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Reorder columns request: tableId={}, user={}", request.getTableId(), username);
        columnService.reorderColumns(request, username);
        return ResponseEntity.ok().build();
    }
}
