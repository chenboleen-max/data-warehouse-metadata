package com.kiro.metadata.repository;

import com.kiro.metadata.entity.ChangeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Change history repository
 */
@Repository
public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, String> {
    
    /**
     * Find change history by entity type and entity ID, ordered by changed time descending
     */
    Page<ChangeHistory> findByEntityTypeAndEntityIdOrderByChangedAtDesc(
            String entityType, String entityId, Pageable pageable);
    
    /**
     * Find change history by user ID, ordered by changed time descending
     */
    Page<ChangeHistory> findByChangedByIdOrderByChangedAtDesc(String userId, Pageable pageable);
}
