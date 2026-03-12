package com.kiro.metadata.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 异步任务配置测试类
 * 验证异步任务线程池配置是否正确
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Slf4j
class AsyncConfigTest {

    /**
     * 测试线程池配置是否正确创建
     */
    @Test
    void testThreadPoolConfiguration() {
        // 创建配置属性
        AsyncProperties asyncProperties = new AsyncProperties();
        asyncProperties.setCorePoolSize(5);
        asyncProperties.setMaxPoolSize(10);
        asyncProperties.setQueueCapacity(100);
        asyncProperties.setThreadNamePrefix("async-task-");

        // 创建配置类
        AsyncConfig asyncConfig = new AsyncConfig(asyncProperties);
        
        // 获取线程池执行器
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();
        
        // 验证线程池配置
        assertThat(taskExecutor).isNotNull();
        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(5);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(10);
        assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("async-task-");
        
        log.info("线程池配置验证通过 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}, 线程名称前缀: {}",
                taskExecutor.getCorePoolSize(),
                taskExecutor.getMaxPoolSize(),
                taskExecutor.getQueueCapacity(),
                taskExecutor.getThreadNamePrefix());
        
        // 关闭线程池
        taskExecutor.shutdown();
    }

    /**
     * 测试异步属性默认值
     */
    @Test
    void testAsyncPropertiesDefaults() {
        AsyncProperties asyncProperties = new AsyncProperties();
        
        assertThat(asyncProperties.getCorePoolSize()).isEqualTo(5);
        assertThat(asyncProperties.getMaxPoolSize()).isEqualTo(10);
        assertThat(asyncProperties.getQueueCapacity()).isEqualTo(100);
        assertThat(asyncProperties.getThreadNamePrefix()).isEqualTo("async-task-");
        
        log.info("异步属性默认值验证通过");
    }

    /**
     * 测试线程池拒绝策略
     */
    @Test
    void testRejectionPolicy() {
        AsyncProperties asyncProperties = new AsyncProperties();
        asyncProperties.setCorePoolSize(2);
        asyncProperties.setMaxPoolSize(2);
        asyncProperties.setQueueCapacity(1);
        asyncProperties.setThreadNamePrefix("test-");

        AsyncConfig asyncConfig = new AsyncConfig(asyncProperties);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();
        
        // 验证拒绝策略是 CallerRunsPolicy
        assertThat(taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
        
        log.info("拒绝策略验证通过: CallerRunsPolicy");
        
        // 关闭线程池
        taskExecutor.shutdown();
    }
}
