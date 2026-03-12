package com.kiro.metadata.service;

import com.kiro.metadata.entity.ChangeHistory;
import com.kiro.metadata.entity.OperationType;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.exception.ResourceNotFoundException;
import com.kiro.metadata.repository.ChangeHistoryRepository;
import com.kiro.metadata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 变更历史服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 记录变更
     */
    @Transactional
    public void recordChange(String entityType, String entityId, String operation,
                            String fieldName, String oldValue, String newValue, String username) {
        log.debug("Recording change: entityType={}, entityId={}, operation={}", 
                 entityType, entityId, operation);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        ChangeHistory history = new ChangeHistory();
        history.setEntityType(entityType);
        history.setEntityId(entityId);
        history.setOperation(OperationType.valueOf(operation.toUpperCase()));
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(user);

        changeHistoryRepository.save(history);
        log.debug("Change recorded successfully");
    }

    /**
     * 获取实体的变更历史
     */
    public Page<ChangeHistory> getEntityHistory(String entityType, String entityId, Pageable pageable) {
        log.debug("Getting entity history: entityType={}, entityId={}", entityType, entityId);
        return changeHistoryRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(
            entityType, entityId, pageable);
    }

    /**
     * 获取用户的操作记录
     */
    public Page<ChangeHistory> getUserActivity(String userId, Pageable pageable) {
        log.debug("Getting user activity: userId={}", userId);
        return changeHistoryRepository.findByChangedByIdOrderByChangedAtDesc(userId, pageable);
    }

    /**
     * 对比版本
     * 简单实现：返回两个版本之间的差异
     */
    public Map<String, Object> compareVersions(String entityId, String version1, String version2) {
        log.info("Comparing versions: entityId={}, v1={}, v2={}", entityId, version1, version2);

        // TODO: 实现版本对比逻辑
        // 这里需要根据实际需求实现版本快照和对比功能
        
        Map<String, Object> diff = new HashMap<>();
        diff.put("entity_id", entityId);
        diff.put("version1", version1);
        diff.put("version2", version2);
        diff.put("differences", new ArrayList<>());

        return diff;
    }
}
