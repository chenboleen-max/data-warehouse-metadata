# Redis 缓存配置文档

## 概述

本文档描述了数据仓库元数据管理系统中 Redis 缓存的配置和使用方法。

## 配置文件

### RedisConfig.java

位置：`src/main/java/com/kiro/metadata/config/RedisConfig.java`

该配置类提供以下功能：

1. **RedisTemplate 配置**：用于存储和操作复杂对象
2. **StringRedisTemplate 配置**：用于存储和操作字符串类型数据
3. **CacheManager 配置**：管理 Spring Cache 注解的缓存行为
4. **Jackson2JsonRedisSerializer 配置**：配置 JSON 序列化和反序列化

### 主要特性

#### 1. 序列化配置

- **Key 序列化**：使用 `StringRedisSerializer`，将 key 存储为字符串
- **Value 序列化**：使用 `Jackson2JsonRedisSerializer`，将对象序列化为 JSON 格式
- **类型信息**：启用默认类型信息，确保反序列化时能正确恢复对象类型
- **时间类型支持**：注册 `JavaTimeModule`，支持 Java 8 时间类型（LocalDateTime, LocalDate 等）

#### 2. 缓存命名策略

缓存 key 格式：`kiro:metadata:{cacheName}::{key}`

示例：
- 表缓存：`kiro:metadata:tables::123e4567-e89b-12d3-a456-426614174000`
- 字段缓存：`kiro:metadata:columns::table:123e4567-e89b-12d3-a456-426614174000`

#### 3. 缓存 TTL 配置

| 缓存名称 | TTL | 说明 |
|---------|-----|------|
| tables | 1 小时 | 表元数据缓存 |
| columns | 1 小时 | 字段元数据缓存 |
| lineage | 30 分钟 | 血缘关系缓存 |
| quality | 15 分钟 | 数据质量指标缓存 |
| catalog | 2 小时 | 数据目录缓存 |
| users | 4 小时 | 用户信息缓存 |
| 默认 | 1 小时 | 其他缓存 |

## 使用方法

### 1. 使用 Spring Cache 注解

#### @Cacheable - 缓存查询结果

```java
@Service
public class MetadataService {
    
    @Cacheable(value = "tables", key = "#tableId")
    public Table getTableById(UUID tableId) {
        return tableRepository.findById(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
    }
    
    @Cacheable(value = "columns", key = "'table:' + #tableId")
    public List<Column> getColumnsByTableId(UUID tableId) {
        return columnRepository.findByTableId(tableId);
    }
}
```

#### @CacheEvict - 清除缓存

```java
@Service
public class MetadataService {
    
    @CacheEvict(value = "tables", key = "#tableId")
    public Table updateTable(UUID tableId, TableUpdateRequest request) {
        Table table = getTableById(tableId);
        // 更新逻辑...
        return tableRepository.save(table);
    }
    
    @CacheEvict(value = {"tables", "columns"}, key = "#tableId")
    public void deleteTable(UUID tableId) {
        tableRepository.deleteById(tableId);
    }
}
```

#### @CachePut - 更新缓存

```java
@Service
public class MetadataService {
    
    @CachePut(value = "tables", key = "#result.id")
    public Table createTable(TableCreateRequest request) {
        Table table = new Table();
        // 设置属性...
        return tableRepository.save(table);
    }
}
```

### 2. 直接使用 RedisTemplate

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
        return Boolean.TRUE.equals(
            stringRedisTemplate.hasKey(key)
        );
    }
}
```

### 3. 使用 RedisTemplate 存储复杂对象

```java
@Service
public class SessionService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void saveSession(String sessionId, UserSession session) {
        String key = "session:" + sessionId;
        redisTemplate.opsForValue().set(key, session, 
            Duration.ofHours(2));
    }
    
    public UserSession getSession(String sessionId) {
        String key = "session:" + sessionId;
        return (UserSession) redisTemplate.opsForValue().get(key);
    }
}
```

## 配置参数

### application-dev.yml

```yaml
spring:
  data:
    redis:
      host: localhost          # Redis 服务器地址
      port: 6379              # Redis 端口
      password:               # Redis 密码（如果有）
      database: 0             # Redis 数据库索引
      lettuce:
        pool:
          max-active: 8       # 连接池最大连接数
          max-idle: 8         # 连接池最大空闲连接数
          min-idle: 0         # 连接池最小空闲连接数
          max-wait: -1ms      # 连接池最大等待时间
```

### application-prod.yml（生产环境）

```yaml
spring:
  data:
    redis:
      host: redis-server      # 生产环境 Redis 地址
      port: 6379
      password: ${REDIS_PASSWORD}  # 从环境变量读取密码
      database: 0
      lettuce:
        pool:
          max-active: 20      # 生产环境增加连接数
          max-idle: 10
          min-idle: 5
          max-wait: 3000ms
      timeout: 3000ms         # 连接超时时间
```

## 缓存策略

### 1. 缓存预热

在应用启动时预加载常用数据：

```java
@Component
@Slf4j
public class CacheWarmer implements ApplicationRunner {
    
    @Autowired
    private MetadataService metadataService;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting cache warming...");
        
        // 预加载热门表元数据
        List<Table> popularTables = metadataService.getPopularTables();
        popularTables.forEach(table -> {
            metadataService.getTableById(table.getId());
        });
        
        log.info("Cache warming completed");
    }
}
```

### 2. 缓存失效策略

- **主动失效**：数据更新时使用 `@CacheEvict` 清除缓存
- **被动失效**：通过 TTL 自动过期
- **级联失效**：删除表时同时清除相关的字段缓存

### 3. 缓存穿透防护

对于不存在的数据，缓存空对象（短 TTL）：

```java
@Cacheable(value = "tables", key = "#tableId", unless = "#result == null")
public Table getTableById(UUID tableId) {
    return tableRepository.findById(tableId).orElse(null);
}
```

## 监控和调试

### 1. 查看缓存统计

使用 Spring Boot Actuator：

```bash
curl http://localhost:8080/actuator/metrics/cache.gets
curl http://localhost:8080/actuator/metrics/cache.puts
```

### 2. Redis 命令行工具

```bash
# 连接 Redis
redis-cli

# 查看所有 key
KEYS kiro:metadata:*

# 查看特定缓存
GET kiro:metadata:tables::123e4567-e89b-12d3-a456-426614174000

# 查看 TTL
TTL kiro:metadata:tables::123e4567-e89b-12d3-a456-426614174000

# 清除所有缓存
FLUSHDB
```

### 3. 日志配置

在 `application.yml` 中启用 Redis 日志：

```yaml
logging:
  level:
    org.springframework.data.redis: DEBUG
    org.springframework.cache: DEBUG
```

## 性能优化建议

1. **合理设置 TTL**：根据数据更新频率设置合适的过期时间
2. **避免缓存大对象**：大对象会占用大量内存，考虑只缓存 ID 列表
3. **使用管道（Pipeline）**：批量操作时使用 Redis Pipeline 提高性能
4. **监控内存使用**：定期检查 Redis 内存使用情况，避免 OOM
5. **使用连接池**：配置合适的连接池参数，避免连接耗尽

## 故障排查

### 问题 1：Redis 连接失败

**症状**：应用启动失败，提示无法连接 Redis

**解决方案**：
1. 检查 Redis 服务是否启动：`redis-cli ping`
2. 检查配置文件中的 host 和 port 是否正确
3. 检查防火墙设置

### 问题 2：序列化错误

**症状**：缓存存储或读取时抛出序列化异常

**解决方案**：
1. 确保实体类实现了 Serializable 接口（使用 JSON 序列化时不需要）
2. 检查 Jackson 配置是否正确
3. 确保实体类有无参构造函数

### 问题 3：缓存未生效

**症状**：数据更新后仍然返回旧数据

**解决方案**：
1. 检查 `@EnableCaching` 注解是否添加
2. 确认方法是通过 Spring 代理调用（不能是内部调用）
3. 检查缓存 key 是否正确

## 测试

### 单元测试

```java
@SpringBootTest
@ActiveProfiles("test")
class RedisConfigTest {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    void testRedisConnection() {
        String key = "test:key";
        String value = "test value";
        
        redisTemplate.opsForValue().set(key, value);
        Object result = redisTemplate.opsForValue().get(key);
        
        assertThat(result).isEqualTo(value);
        
        redisTemplate.delete(key);
    }
}
```

### 集成测试

使用 Testcontainers 启动 Redis 容器进行测试：

```java
@SpringBootTest
@Testcontainers
class CacheIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    
    // 测试方法...
}
```

## 参考资料

- [Spring Data Redis 官方文档](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Spring Cache 抽象](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Redis 官方文档](https://redis.io/documentation)
- [Lettuce Redis 客户端](https://lettuce.io/)
