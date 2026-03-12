package com.kiro.metadata.entity;

/**
 * 任务状态枚举
 * 定义导出任务的状态
 */
public enum TaskStatus {
    /**
     * 任务等待执行
     */
    PENDING,
    
    /**
     * 任务正在运行
     */
    RUNNING,
    
    /**
     * 任务成功完成
     */
    COMPLETED,
    
    /**
     * 任务失败并出错
     */
    FAILED
}
