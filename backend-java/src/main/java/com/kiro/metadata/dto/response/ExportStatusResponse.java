package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.ExportTask;
import com.kiro.metadata.entity.ExportType;
import com.kiro.metadata.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * å¯¼åºç¶æååº?DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportStatusResponse {
    
    private String id;
    private ExportType taskType;
    private TaskStatus status;
    private String filePath;
    private Integer recordCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private UserResponse createdBy;
    
    /**
     * ä»?ExportTask å®ä½è½¬æ¢ä¸?ExportStatusResponse
     */
    public static ExportStatusResponse from(ExportTask task) {
        return ExportStatusResponse.builder()
                .id(task.getId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .filePath(task.getFilePath())
                .recordCount(task.getRecordCount())
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .createdBy(task.getCreatedBy() != null ? UserResponse.from(task.getCreatedBy()) : null)
                .build();
    }
}

