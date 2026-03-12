package com.kiro.metadata.repository;

import com.kiro.metadata.entity.ColumnMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Column metadata repository
 */
@Repository
public interface ColumnRepository extends JpaRepository<ColumnMetadata, String> {
    
    /**
     * Find columns by table ID ordered by column order
     */
    List<ColumnMetadata> findByTableIdOrderByColumnOrder(String tableId);
}
