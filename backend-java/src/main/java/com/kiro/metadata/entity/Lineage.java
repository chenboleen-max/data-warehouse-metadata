package com.kiro.metadata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 血缘关系实体
 * 表示两个表之间的血缘关系
 */
@Entity
@Table(name = "lineage",
       uniqueConstraints = @UniqueConstraint(columnNames = {"source_table_id", "target_table_id"}),
       indexes = {
           @Index(name = "idx_source", columnList = "source_table_id"),
           @Index(name = "idx_target", columnList = "target_table_id")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lineage {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_table_id", nullable = false)
    private TableMetadata sourceTable;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_table_id", nullable = false)
    private TableMetadata targetTable;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lineage_type", nullable = false, length = 20)
    private LineageType lineageType;
    
    @Column(name = "transformation_logic", columnDefinition = "TEXT")
    private String transformationLogic;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
