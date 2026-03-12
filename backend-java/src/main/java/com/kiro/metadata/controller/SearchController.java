package com.kiro.metadata.controller;

import com.kiro.metadata.dto.request.SearchRequest;
import com.kiro.metadata.dto.response.PagedResponse;
import com.kiro.metadata.dto.response.TableResponse;
import com.kiro.metadata.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "搜索", description = "全文搜索接口")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "全文搜索", description = "搜索表元数据")
    @ApiResponse(responseCode = "200", description = "搜索成功")
    public ResponseEntity<PagedResponse<TableResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Search request: keyword={}, page={}, size={}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TableResponse> resultPage = searchService.searchTables(keyword, pageable);
        
        PagedResponse<TableResponse> response = PagedResponse.<TableResponse>builder()
            .items(resultPage.getContent())
            .page(resultPage.getNumber())
            .pageSize(resultPage.getSize())
            .total(resultPage.getTotalElements())
            .totalPages(resultPage.getTotalPages())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggest")
    @Operation(summary = "搜索建议", description = "获取搜索自动补全建议")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<List<String>> suggest(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Suggest request: prefix={}, limit={}", prefix, limit);
        List<String> suggestions = searchService.suggest(prefix, limit);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/filter")
    @Operation(summary = "高级过滤", description = "使用多个条件过滤表")
    @ApiResponse(responseCode = "200", description = "过滤成功")
    public ResponseEntity<PagedResponse<TableResponse>> filter(
            @Valid @RequestBody SearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Filter request: {}", request);
        Pageable pageable = PageRequest.of(page, size);
        Page<TableResponse> resultPage = searchService.filterTables(request, pageable);
        
        PagedResponse<TableResponse> response = PagedResponse.<TableResponse>builder()
            .items(resultPage.getContent())
            .page(resultPage.getNumber())
            .pageSize(resultPage.getSize())
            .total(resultPage.getTotalElements())
            .totalPages(resultPage.getTotalPages())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
