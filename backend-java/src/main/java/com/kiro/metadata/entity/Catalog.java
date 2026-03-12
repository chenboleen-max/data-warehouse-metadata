package com.kiro.metadata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 目录实体
 * Represents a catalog node in the hierarchical data catalog
 */
@Entity
@Table(name = "catalog",
       indexes = {
           @Index(name = "idx_parent", columnList = "parent_id"),
           @Index(name = "idx_path", columnList = "path")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catalog {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Catalog parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Catalog> children = new ArrayList<>();
    
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer level;
    
    @Column(nullable = false, length = 500)
    private String path;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToMany(mappedBy = "catalogs")
    private Set<TableMetadata> tables = new HashSet<>();
}
