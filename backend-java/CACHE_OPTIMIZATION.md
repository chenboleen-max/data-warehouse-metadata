# 缓存优化配置指南

## Redis 缓存策略

### 已实现的缓存

#### 1. 表元数据缓存
- **缓存键**: `table:{id}`
- **TTL**: 1小时
- **注解**: `@Cacheable(value = "table", key = "#id")`
- **失效策略**: 更新或删除表时自动清除

```java
@Cacheable(value = "table", key = "#id")
public TableMetadata getTableById(UUID id) {
    // ...
}

@CacheEvict(value = "table", key = "#id")
public void updateTable(UUID id, TableUpdateRequest request) {
    // ...
}
```

#### 2. 字段元数据缓存
- **缓存键**: `columns:table:{tableId}`
- **TTL**: 1小时
- **注解**: `@Cacheable(value = "columns", key = "'table:' + #tableId")`
- **失效策略**: 字段增删改时清除对应表的缓存

```java
@Cacheable(value = "columns", key = "'table:' + #tableId")
public List<Column> getColumnsByTableId(UUID tableId) {
    // ...
}

@CacheEvict(value = "columns", key = "'table:' + #column.tableId")
public void createColumn(ColumnCreateRequest request) {
    // ...
}
```

#### 3. JWT Token 黑名单
- **缓存键**: `token:blacklist:{token}`
- **TTL**: Token 过期时间
- **用途**: 实现登出功能

```java
public void logout(String token) {
    String username = jwtTokenProvider.getUsernameFromToken(token);
    long expirationTime = jwtTokenProvider.getExpirationTime(token);
    redisTemplate.opsForValue().set(
        "token:blacklist:" + token,
        username,
        expirationTime,
        TimeUnit.MILLISECONDS
    );
}
```

### 缓存配置优化

#### application.yml 配置

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "metadata:"
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms
```

### 缓存预热

在应用启动时预加载热点数据：

```java
@Component
public class CacheWarmer {
    
    @Autowired
    private MetadataService metadataService;
    
    @Autowired
    private TableRepository tableRepository;
    
    @PostConstruct
    public void warmUpCache() {
        log.info("开始缓存预热...");
        
        // 预加载最近更新的 100 个表
        List<TableMetadata> recentTables = tableRepository.selectList(
            new QueryWrapper<TableMetadata>()
                .orderByDesc("updated_at")
                .last("LIMIT 100")
        );
        
        for (TableMetadata table : recentTables) {
            metadataService.getTableById(table.getId());
        }
        
        log.info("缓存预热完成，预加载了 {} 个表", recentTables.size());
    }
}
```

### 缓存监控

使用 Spring Boot Actuator 监控缓存状态：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
  metrics:
    enable:
      cache: true
```

访问缓存指标：
- `GET /actuator/caches` - 查看所有缓存
- `GET /actuator/metrics/cache.gets` - 缓存命中率
- `GET /actuator/metrics/cache.puts` - 缓存写入次数
- `GET /actuator/metrics/cache.evictions` - 缓存驱逐次数

### 缓存最佳实践

#### 1. 合理设置 TTL
- 频繁变更的数据：5-15分钟
- 中等变更频率：30-60分钟
- 很少变更的数据：2-24小时

#### 2. 避免缓存穿透
```java
@Cacheable(value = "table", key = "#id", unless = "#result == null")
public TableMetadata getTableById(UUID id) {
    // 如果返回 null，不缓存
}
```

#### 3. 避免缓存雪崩
- 设置随机 TTL：`TTL + random(0, 300)`
- 使用互斥锁防止并发查询

#### 4. 避免缓存击穿
```java
@Cacheable(value = "table", key = "#id", sync = true)
public TableMetadata getTableById(UUID id) {
    // sync = true 启用同步模式，防止缓存击穿
}
```

### 缓存清理策略

#### 1. 手动清理
```java
@CacheEvict(value = "table", allEntries = true)
public void clearAllTableCache() {
    log.info("清除所有表缓存");
}
```

#### 2. 定时清理
```java
@Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点
@CacheEvict(value = {"table", "columns"}, allEntries = true)
public void scheduledCacheClear() {
    log.info("定时清理缓存");
}
```

### 性能指标

#### 目标
- 缓存命中率 > 80%
- 平均响应时间 < 100ms（缓存命中）
- 平均响应时间 < 500ms（缓存未命中）

#### 监控命令
```bash
# Redis 监控
redis-cli INFO stats
redis-cli INFO memory

# 查看缓存键数量
redis-cli DBSIZE

# 查看特定前缀的键
redis-cli KEYS "metadata:*"
```

## 二级缓存（可选）

如果需要更高的性能，可以启用 JPA 二级缓存：

```yaml
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
```

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TableMetadata {
    // ...
}
```

## 总结

缓存优化已在以下服务中实现：
- ✅ MetadataService（表元数据）
- ✅ ColumnService（字段元数据）
- ✅ AuthService（Token 黑名单）

建议监控缓存命中率，根据实际情况调整 TTL 和缓存策略。
