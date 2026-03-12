package com.kiro.metadata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * 变更历史实体
 * Tracks changes to metadata entities
 */
@Entity
@Table(name = "change_history",
       indexes = @Index(name = "idx_entity_changed", 
                       columnList = "entity_type, entity_id, changed_at"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeHistory {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;
    
    @Column(name = "entity_id", nullable = false, length = 36)
    private String entityId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationType operation;
    
    @Column(name = "field_name", length = 100)
    private String fieldName;
    
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;
}
