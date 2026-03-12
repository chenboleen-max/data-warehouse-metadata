package com.kiro.metadata.repository;

import com.kiro.metadata.entity.QualityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Quality metrics repository
 */
@Repository
public interface QualityMetricsRepository extends JpaRepository<QualityMetrics, String> {
    
    /**
     * Find the latest quality metrics for a table
     */
    Optional<QualityMetrics> findFirstByTableIdOrderByMeasuredAtDesc(String tableId);
    
    /**
     * Find quality metrics for a table after a specific date
     */
    List<QualityMetrics> findByTableIdAndMeasuredAtAfterOrderByMeasuredAtDesc(String tableId, LocalDateTime after);
}
