package com.kiro.metadata.dto.response;

import com.kiro.metadata.entity.ChangeHistory;
import com.kiro.metadata.entity.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 鍙樻洿鍘嗗彶鍝嶅簲 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeHistoryResponse {
    
    private String id;
    private String entityType;
    private String entityId;
    private OperationType operation;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
    private UserResponse changedBy;
    
    /**
     * 浠?ChangeHistory 瀹炰綋杞崲涓?ChangeHistoryResponse
     */
    public static ChangeHistoryResponse from(ChangeHistory history) {
        return ChangeHistoryResponse.builder()
                .id(history.getId())
                .entityType(history.getEntityType())
                .entityId(history.getEntityId())
                .operation(history.getOperation())
                .fieldName(history.getFieldName())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .changedAt(history.getChangedAt())
                .changedBy(history.getChangedBy() != null ? UserResponse.from(history.getChangedBy()) : null)
                .build();
    }
}

