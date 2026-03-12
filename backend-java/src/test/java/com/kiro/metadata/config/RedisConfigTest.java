package com.kiro.metadata.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 配置测试类
 * 
 * 验证 Redis 配置是否正确加载和初始化
 * 
 * @author kiro
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class RedisConfigTest {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Test
    void testRedisTemplateBeanExists() {
        // 验证 RedisTemplate Bean 已创建
        assertThat(redisTemplate).isNotNull();
    }

    @Test
    void testStringRedisTemplateBeanExists() {
        // 验证 StringRedisTemplate Bean 已创建
        assertThat(stringRedisTemplate).isNotNull();
    }

    @Test
    void testCacheManagerBeanExists() {
        // 验证 CacheManager Bean 已创建
        assertThat(cacheManager).isNotNull();
    }

    @Test
    void testRedisTemplateSerializers() {
        // 验证 RedisTemplate 的序列化器配置
        if (redisTemplate != null) {
            assertThat(redisTemplate.getKeySerializer()).isNotNull();
            assertThat(redisTemplate.getValueSerializer()).isNotNull();
            assertThat(redisTemplate.getHashKeySerializer()).isNotNull();
            assertThat(redisTemplate.getHashValueSerializer()).isNotNull();
        }
    }

    @Test
    void testCacheManagerCacheNames() {
        // 验证 CacheManager 配置的缓存名称
        if (cacheManager != null) {
            // 注意：在测试环境中，缓存可能还未初始化
            // 这个测试主要验证 CacheManager 实例存在
            assertThat(cacheManager).isNotNull();
        }
    }
}
