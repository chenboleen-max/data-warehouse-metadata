package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.QualityMetrics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 鏁版嵁璐ㄩ噺鎸囨爣鍝嶅簲 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityMetricsResponse {
    
    private String id;
    private String tableId;
    private Long recordCount;
    private Double nullRate;
    private String updateFrequency;
    private Integer dataFreshnessHours;
    private LocalDateTime measuredAt;
    private LocalDateTime createdAt;
    
    /**
     * 浠?QualityMetrics 瀹炰綋杞崲涓?QualityMetricsResponse
     */
    public static QualityMetricsResponse from(QualityMetrics metrics) {
        return QualityMetricsResponse.builder()
                .id(metrics.getId())
                .tableId(metrics.getTable() != null ? metrics.getTable().getId() : null)
                .recordCount(metrics.getRecordCount())
                .nullRate(metrics.getNullRate())
                .updateFrequency(metrics.getUpdateFrequency())
                .dataFreshnessHours(metrics.getDataFreshnessHours())
                .measuredAt(metrics.getMeasuredAt())
                .createdAt(metrics.getCreatedAt())
                .build();
    }
}

