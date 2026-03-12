package com.kiro.metadata.repository;

import com.kiro.metadata.entity.Lineage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Lineage repository
 */
@Repository
public interface LineageRepository extends JpaRepository<Lineage, String> {
    
    /**
     * Find lineages by source table ID
     */
    List<Lineage> findBySourceTableId(String sourceTableId);
    
    /**
     * Find lineages by target table ID
     */
    List<Lineage> findByTargetTableId(String targetTableId);
    
    /**
     * Check if lineage exists by source and target table IDs
     */
    boolean existsBySourceTableIdAndTargetTableId(String sourceTableId, String targetTableId);
}
