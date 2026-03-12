# Redis 缓存快速入门指南

## 快速开始

### 1. 启动 Redis 服务

#### 使用 Docker（推荐）

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

#### 使用 Docker Compose

在项目根目录的 `docker-compose.yml` 中已包含 Redis 配置：

```bash
cd kiro_web/backend-java
docker-compose up -d redis
```

### 2. 验证 Redis 连接

```bash
# 使用 redis-cli 连接
redis-cli ping
# 应该返回：PONG

# 或使用 Docker
docker exec -it redis redis-cli ping
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用启动后，Redis 缓存配置会自动加载。

## 常用缓存注解

### @Cacheable - 缓存查询结果

```java
@Cacheable(value = "tables", key = "#tableId")
public Table getTableById(UUID tableId) {
    return tableRepository.findById(tableId)
        .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
}
```

**说明**：
- `value`：缓存空间名称
- `key`：缓存键，支持 SpEL 表达式
- 第一次调用时执行方法并缓存结果
- 后续调用直接从缓存返回

### @CacheEvict - 清除缓存

```java
@CacheEvict(value = "tables", key = "#tableId")
public void deleteTable(UUID tableId) {
    tableRepository.deleteById(tableId);
}
```

**说明**：
- 执行方法后清除指定缓存
- `allEntries = true`：清除该缓存空间的所有条目

### @CachePut - 更新缓存

```java
@CachePut(value = "tables", key = "#result.id")
public Table createTable(TableCreateRequest request) {
    Table table = new Table();
    // 设置属性...
    return tableRepository.save(table);
}
```

**说明**：
- 每次都执行方法并更新缓存
- 适用于创建和更新操作

## 直接使用 RedisTemplate

### 存储和获取字符串

```java
@Autowired
private StringRedisTemplate stringRedisTemplate;

// 存储
stringRedisTemplate.opsForValue().set("key", "value", Duration.ofMinutes(30));

// 获取
String value = stringRedisTemplate.opsForValue().get("key");

// 删除
stringRedisTemplate.delete("key");
```

### 存储和获取对象

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// 存储对象
UserSession session = new UserSession();
redisTemplate.opsForValue().set("session:123", session, Duration.ofHours(2));

// 获取对象
UserSession retrieved = (UserSession) redisTemplate.opsForValue().get("session:123");
```

## 缓存命名规范

### 缓存空间命名

| 缓存空间 | 用途 | TTL |
|---------|------|-----|
| tables | 表元数据 | 1 小时 |
| columns | 字段元数据 | 1 小时 |
| lineage | 血缘关系 | 30 分钟 |
| quality | 数据质量指标 | 15 分钟 |
| catalog | 数据目录 | 2 小时 |
| users | 用户信息 | 4 小时 |

### 缓存键命名

```java
// 单个实体：使用 ID
@Cacheable(value = "tables", key = "#id")

// 列表查询：使用组合键
@Cacheable(value = "columns", key = "'table:' + #tableId")

// 复杂查询：使用参数组合
@Cacheable(value = "search", key = "#keyword + ':' + #page + ':' + #size")
```

## 常见使用场景

### 1. Token 黑名单

```java
@Service
public class TokenBlacklistService {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    public void addToBlacklist(String token, long expirationTime) {
        String key = "blacklist:token:" + token;
        stringRedisTemplate.opsForValue().set(key, "1", 
            Duration.ofMillis(expirationTime));
    }
    
    public boolean isBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
```

### 2. 用户会话

```java
@Service
public class SessionService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void saveSession(String sessionId, UserSession session) {
        String key = "session:" + sessionId;
        redisTemplate.opsForValue().set(key, session, Duration.ofHours(2));
    }
    
    public UserSession getSession(String sessionId) {
        String key = "session:" + sessionId;
        return (UserSession) redisTemplate.opsForValue().get(key);
    }
}
```

### 3. 计数器

```java
@Service
public class CounterService {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    public long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
    
    public long getCount(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }
}
```

### 4. 分布式锁

```java
@Service
public class DistributedLockService {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        Boolean result = stringRedisTemplate.opsForValue()
            .setIfAbsent(lockKey, requestId, Duration.ofSeconds(expireTime));
        return Boolean.TRUE.equals(result);
    }
    
    public void unlock(String lockKey, String requestId) {
        String value = stringRedisTemplate.opsForValue().get(lockKey);
        if (requestId.equals(value)) {
            stringRedisTemplate.delete(lockKey);
        }
    }
}
```

## 调试技巧

### 1. 查看缓存内容

```bash
# 连接 Redis
redis-cli

# 查看所有缓存 key
KEYS kiro:metadata:*

# 查看特定缓存
GET kiro:metadata:tables::123e4567-e89b-12d3-a456-426614174000

# 查看 TTL
TTL kiro:metadata:tables::123e4567-e89b-12d3-a456-426614174000
```

### 2. 清除缓存

```bash
# 清除特定 key
DEL kiro:metadata:tables::123e4567-e89b-12d3-a456-426614174000

# 清除所有表缓存
KEYS kiro:metadata:tables::* | xargs redis-cli DEL

# 清除所有缓存（谨慎使用）
FLUSHDB
```

### 3. 监控缓存命中率

```bash
# 查看 Redis 统计信息
INFO stats

# 查看缓存命中率
redis-cli INFO stats | grep keyspace
```

### 4. 启用日志

在 `application-dev.yml` 中添加：

```yaml
logging:
  level:
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG
```

## 性能优化建议

### 1. 合理设置 TTL

```java
// 频繁变化的数据：短 TTL
@Cacheable(value = "quality", key = "#tableId")  // 15 分钟

// 稳定的数据：长 TTL
@Cacheable(value = "catalog", key = "#id")  // 2 小时
```

### 2. 避免缓存大对象

```java
// 不推荐：缓存完整的大对象
@Cacheable(value = "tables", key = "#id")
public TableWithAllDetails getTableWithAllDetails(UUID id) {
    // 包含大量字段和关联数据
}

// 推荐：只缓存必要的数据
@Cacheable(value = "tables", key = "#id")
public TableBasicInfo getTableBasicInfo(UUID id) {
    // 只包含基本信息
}
```

### 3. 使用条件缓存

```java
// 只缓存非空结果
@Cacheable(value = "tables", key = "#id", unless = "#result == null")
public Table getTableById(UUID id) {
    return tableRepository.findById(id).orElse(null);
}

// 根据条件决定是否缓存
@Cacheable(value = "tables", key = "#id", condition = "#cacheable == true")
public Table getTableById(UUID id, boolean cacheable) {
    return tableRepository.findById(id).orElse(null);
}
```

## 常见问题

### Q1: 缓存不生效？

**检查清单**：
1. 确认 `@EnableCaching` 注解已添加到配置类
2. 确认方法是通过 Spring 代理调用（不能是类内部调用）
3. 检查缓存 key 是否正确
4. 查看日志确认缓存操作是否执行

### Q2: 如何清除所有缓存？

```java
@Autowired
private CacheManager cacheManager;

public void clearAllCaches() {
    cacheManager.getCacheNames().forEach(cacheName -> {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    });
}
```

### Q3: 如何在测试中禁用缓存？

在测试配置中：

```yaml
spring:
  cache:
    type: none
```

或使用注解：

```java
@SpringBootTest
@EnableAutoConfiguration(exclude = {CacheAutoConfiguration.class})
class MyTest {
    // 测试代码
}
```

## 下一步

- 阅读完整文档：[REDIS_CONFIGURATION.md](../REDIS_CONFIGURATION.md)
- 查看示例代码：[CacheExampleService.java](../src/main/java/com/kiro/metadata/service/example/CacheExampleService.java)
- 运行测试：`mvn test -Dtest=RedisConfigTest`

## 参考资料

- [Spring Cache 官方文档](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Spring Data Redis 官方文档](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Redis 命令参考](https://redis.io/commands)
