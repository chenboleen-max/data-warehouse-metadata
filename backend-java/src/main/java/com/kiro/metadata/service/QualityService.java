package com.kiro.metadata.service;

import com.kiro.metadata.entity.QualityMetrics;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.exception.ResourceNotFoundException;
import com.kiro.metadata.repository.QualityMetricsRepository;
import com.kiro.metadata.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据质量服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QualityService {

    private final QualityMetricsRepository qualityMetricsRepository;
    private final TableRepository tableRepository;

    /**
     * 记录质量指标
     */
    @Transactional
    public QualityMetrics recordQualityMetrics(String tableId, Long recordCount, 
                                              Double nullRate, String updateFrequency, 
                                              Integer dataFreshnessHours) {
        log.info("Recording quality metrics for table: {}", tableId);

        TableMetadata table = tableRepository.findById(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("表不存在: " + tableId));

        QualityMetrics metrics = new QualityMetrics();
        metrics.setTable(table);
        metrics.setRecordCount(recordCount);
        metrics.setNullRate(nullRate);
        metrics.setUpdateFrequency(updateFrequency);
        metrics.setDataFreshnessHours(dataFreshnessHours);
        metrics.setMeasuredAt(LocalDateTime.now());
        metrics.setCreatedAt(LocalDateTime.now());

        QualityMetrics saved = qualityMetricsRepository.save(metrics);
        log.info("Quality metrics recorded successfully: id={}", saved.getId());

        return saved;
    }

    /**
     * 获取表的最新质量指标
     */
    public QualityMetrics getQualityMetrics(String tableId) {
        log.debug("Getting quality metrics for table: {}", tableId);

        return qualityMetricsRepository.findFirstByTableIdOrderByMeasuredAtDesc(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("质量指标不存在: " + tableId));
    }

    /**
     * 获取质量趋势
     */
    public List<QualityMetrics> getQualityTrend(String tableId, int days) {
        log.debug("Getting quality trend for table: {}, days={}", tableId, days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return qualityMetricsRepository.findByTableIdAndMeasuredAtAfterOrderByMeasuredAtDesc(
            tableId, startDate);
    }

    /**
     * 计算质量分数
     * 简单实现：基于空值率计算
     */
    public double calculateQualityScore(String tableId) {
        log.debug("Calculating quality score for table: {}", tableId);

        QualityMetrics metrics = getQualityMetrics(tableId);
        
        // 质量分数 = 100 - (空值率 * 100)
        double score = 100.0 - (metrics.getNullRate() * 100.0);
        
        // 确保分数在 0-100 之间
        return Math.max(0, Math.min(100, score));
    }
}
