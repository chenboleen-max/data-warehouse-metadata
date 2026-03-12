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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 表元数据实体
 * Represents a table in the data warehouse
 */
@Entity
@Table(name = "tables",
       uniqueConstraints = @UniqueConstraint(columnNames = {"database_name", "table_name"}),
       indexes = {
           @Index(name = "idx_updated_at", columnList = "updated_at"),
           @Index(name = "idx_database_name", columnList = "database_name")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    
    @Column(name = "database_name", nullable = false, length = 100)
    private String databaseName;
    
    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", nullable = false, length = 20)
    private TableType tableType;
    
    @Column(length = 1000)
    private String description;
    
    // Storage information
    @Column(name = "storage_format", length = 50)
    private String storageFormat;
    
    @Column(name = "storage_location", length = 500)
    private String storageLocation;
    
    @Column(name = "data_size_bytes")
    private Long dataSizeBytes;
    
    // Time information
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColumnMetadata> columns = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "table_catalog",
        joinColumns = @JoinColumn(name = "table_id"),
        inverseJoinColumns = @JoinColumn(name = "catalog_id")
    )
    private Set<Catalog> catalogs = new HashSet<>();
}
