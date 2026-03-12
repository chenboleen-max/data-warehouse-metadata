package com.kiro.metadata.repository;

import com.kiro.metadata.entity.TableMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Table metadata repository
 */
@Repository
public interface TableRepository extends JpaRepository<TableMetadata, String> {
    
    /**
     * Find table by database name and table name
     */
    Optional<TableMetadata> findByDatabaseNameAndTableName(String databaseName, String tableName);
    
    /**
     * Check if table exists by database name and table name
     */
    boolean existsByDatabaseNameAndTableName(String databaseName, String tableName);
    
    /**
     * Find tables by database name
     */
    Page<TableMetadata> findByDatabaseName(String databaseName, Pageable pageable);
}
