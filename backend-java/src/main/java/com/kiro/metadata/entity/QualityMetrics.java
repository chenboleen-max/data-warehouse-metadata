package com.kiro.metadata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Quality metrics entity
 * Represents data quality metrics for a table
 */
@Entity
@Table(name = "quality_metrics",
       indexes = @Index(name = "idx_table_measured", columnList = "table_id, measured_at"))
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityMetrics {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private TableMetadata table;
    
    @Column(name = "record_count")
    private Long recordCount;
    
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "null_rate")
    private Double nullRate;
    
    @Column(name = "update_frequency", length = 20)
    private String updateFrequency;
    
    @Column(name = "data_freshness_hours")
    private Integer dataFreshnessHours;
    
    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
