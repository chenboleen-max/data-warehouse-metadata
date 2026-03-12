package com.kiro.metadata.repository;

import com.kiro.metadata.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 目录仓库
 */
@Repository
public interface CatalogRepository extends JpaRepository<Catalog, String> {
    
    /**
     * Find root catalogs (catalogs without parent)
     */
    List<Catalog> findByParentIsNull();
}
