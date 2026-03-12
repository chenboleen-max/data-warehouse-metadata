package com.kiro.metadata.repository;

import com.kiro.metadata.entity.ExportTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 导出任务仓库
 */
@Repository
public interface ExportTaskRepository extends JpaRepository<ExportTask, String> {
}
