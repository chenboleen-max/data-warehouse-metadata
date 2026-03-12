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
 * 字段元数据实体
 * Represents a column in a table
 */
@Entity
@Table(name = "columns",
       indexes = {
           @Index(name = "idx_table_order", columnList = "table_id, column_order"),
           @Index(name = "idx_table_name", columnList = "table_id, column_name")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private TableMetadata table;
    
    @Column(name = "column_name", nullable = false, length = 100)
    private String columnName;
    
    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;
    
    @Column(name = "column_order", nullable = false)
    private Integer columnOrder;
    
    @Column(name = "is_nullable", nullable = false)
    private Boolean isNullable = true;
    
    @Column(name = "is_partition_key", nullable = false)
    private Boolean isPartitionKey = false;
    
    @Column(length = 1000)
    private String description;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
