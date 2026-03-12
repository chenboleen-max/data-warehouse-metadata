# 数据库优化指南

## 索引优化

### 已实现的索引

所有索引已在实体类中通过 JPA 注解配置：

#### 1. TableMetadata 表
```java
@Table(name = "table_metadata", indexes = {
    @Index(name = "idx_updated_at", columnList = "updated_at"),
    @Index(name = "idx_database_table", columnList = "database_name, table_name", unique = true)
})
```

- `idx_updated_at`: 支持按更新时间排序和过滤
- `idx_database_table`: 唯一索引，支持按数据库名和表名查询

#### 2. Column 表
```java
@Table(name = "column_metadata", indexes = {
    @Index(name = "idx_table_order", columnList = "table_id, column_order")
})
```

- `idx_table_order`: 支持按表 ID 和字段顺序查询

#### 3. Lineage 表
```java
@Table(name = "lineage", indexes = {
    @Index(name = "idx_source", columnList = "source_table_id"),
    @Index(name = "idx_target", columnList = "target_table_id")
})
```

- `idx_source`: 支持查询上游表
- `idx_target`: 支持查询下游表

#### 4. Catalog 表
```java
@Table(name = "catalog", indexes = {
    @Index(name = "idx_parent", columnList = "parent_id"),
    @Index(name = "idx_path", columnList = "path")
})
```

- `idx_parent`: 支持查询子目录
- `idx_path`: 支持路径查询

#### 5. QualityMetrics 表
```java
@Table(name = "quality_metrics", indexes = {
    @Index(name = "idx_table_measured", columnList = "table_id, measured_at")
})
```

- `idx_table_measured`: 支持按表 ID 和测量时间查询

#### 6. ChangeHistory 表
```java
@Table(name = "change_history", indexes = {
    @Index(name = "idx_entity_changed", columnList = "entity_type, entity_id, changed_at")
})
```

- `idx_entity_changed`: 支持按实体类型、实体 ID 和变更时间查询

#### 7. ExportTask 表
```java
@Table(name = "export_task", indexes = {
    @Index(name = "idx_created", columnList = "created_at"),
    @Index(name = "idx_status", columnList = "status")
})
```

- `idx_created`: 支持按创建时间排序
- `idx_status`: 支持按状态过滤

### 索引使用建议

#### 1. 查询优化
使用 EXPLAIN 分析查询计划：

```sql
EXPLAIN SELECT * FROM table_metadata 
WHERE database_name = 'default' 
AND table_name = 'users';
```

#### 2. 避免全表扫描
- 在 WHERE 子句中使用索引列
- 避免在索引列上使用函数
- 避免使用 `!=` 或 `<>` 操作符

#### 3. 复合索引顺序
- 将选择性高的列放在前面
- 遵循最左前缀原则

## HikariCP 连接池优化

### 配置参数

```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      # 连接池配置
      minimum-idle: 10                    # 最小空闲连接数
      maximum-pool-size: 50               # 最大连接数
      connection-timeout: 30000           # 连接超时时间（毫秒）
      idle-timeout: 600000                # 空闲连接超时时间（10分钟）
      max-lifetime: 1800000               # 连接最大生命周期（30分钟）
      
      # 连接测试
      connection-test-query: SELECT 1
      validation-timeout: 5000
      
      # 性能优化
      auto-commit: true
      pool-name: MetadataHikariPool
      
      # 泄漏检测
      leak-detection-threshold: 60000     # 连接泄漏检测阈值（1分钟）
```

### 连接池大小计算

推荐公式：
```
connections = ((core_count * 2) + effective_spindle_count)
```

示例：
- 4核CPU + 1个磁盘 = (4 * 2) + 1 = 9个连接
- 建议设置为 10-20 个连接

### 监控连接池

```java
@Component
public class HikariMetrics {
    
    @Autowired
    private HikariDataSource dataSource;
    
    @Scheduled(fixedRate = 60000)  // 每分钟
    public void logPoolStats() {
        HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        log.info("HikariCP Stats - Active: {}, Idle: {}, Total: {}, Waiting: {}",
            poolMXBean.getActiveConnections(),
            poolMXBean.getIdleConnections(),
            poolMXBean.getTotalConnections(),
            poolMXBean.getThreadsAwaitingConnection()
        );
    }
}
```

## MySQL 配置优化

### my.cnf 配置

```ini
[mysqld]
# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 连接配置
max_connections = 200
max_connect_errors = 1000
wait_timeout = 28800
interactive_timeout = 28800

# InnoDB 配置
innodb_buffer_pool_size = 1G          # 设置为物理内存的 50-70%
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M
innodb_flush_log_at_trx_commit = 2    # 性能优化，可接受少量数据丢失
innodb_flush_method = O_DIRECT

# 查询缓存（MySQL 5.7）
query_cache_type = 1
query_cache_size = 64M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2

# 二进制日志
log_bin = /var/log/mysql/mysql-bin.log
expire_logs_days = 7
max_binlog_size = 100M
```

### 慢查询分析

#### 1. 启用慢查询日志
```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

#### 2. 分析慢查询
```bash
# 使用 mysqldumpslow 分析
mysqldumpslow -s t -t 10 /var/log/mysql/slow.log

# 使用 pt-query-digest（推荐）
pt-query-digest /var/log/mysql/slow.log
```

#### 3. 优化慢查询
- 添加索引
- 重写查询
- 分页查询
- 避免 SELECT *

## JPA 查询优化

### 1. N+1 查询问题

#### 问题示例
```java
// 会产生 N+1 查询
List<TableMetadata> tables = tableRepository.findAll();
for (TableMetadata table : tables) {
    List<Column> columns = table.getColumns();  // 每次都查询数据库
}
```

#### 解决方案
```java
// 使用 JOIN FETCH
@Query("SELECT t FROM TableMetadata t LEFT JOIN FETCH t.columns WHERE t.id = :id")
TableMetadata findByIdWithColumns(@Param("id") UUID id);

// 或使用 EntityGraph
@EntityGraph(attributePaths = {"columns"})
List<TableMetadata> findAll();
```

### 2. 批量操作

```java
// 使用 MyBatis-Plus 批量插入
metadataService.saveBatch(tables, 1000);  // 每批 1000 条

// 使用 JDBC 批量更新
jdbcTemplate.batchUpdate(sql, batchArgs);
```

### 3. 分页查询

```java
// 使用 MyBatis-Plus 分页
Page<TableMetadata> page = new Page<>(1, 20);
IPage<TableMetadata> result = tableRepository.selectPage(page, queryWrapper);
```

### 4. 只查询需要的字段

```java
// 不要使用 SELECT *
@Query("SELECT new com.kiro.metadata.dto.TableSummary(t.id, t.tableName, t.databaseName) " +
       "FROM TableMetadata t")
List<TableSummary> findAllSummaries();
```

## 数据库连接优化

### 1. 使用连接池
- ✅ 已配置 HikariCP
- ✅ 连接复用
- ✅ 连接泄漏检测

### 2. 事务管理
```java
@Transactional(readOnly = true)  // 只读事务，性能更好
public List<TableMetadata> listTables() {
    // ...
}

@Transactional(timeout = 30)  // 设置超时时间
public void updateTable() {
    // ...
}
```

### 3. 批量提交
```java
@Transactional
public void batchInsert(List<TableMetadata> tables) {
    for (int i = 0; i < tables.size(); i++) {
        tableRepository.insert(tables.get(i));
        if (i % 100 == 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

## 性能监控

### 1. Spring Boot Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,datasource
  metrics:
    enable:
      jdbc: true
```

访问指标：
- `GET /actuator/metrics/jdbc.connections.active`
- `GET /actuator/metrics/jdbc.connections.max`
- `GET /actuator/metrics/hikaricp.connections`

### 2. 数据库监控

```sql
-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看最大连接数
SHOW VARIABLES LIKE 'max_connections';

-- 查看慢查询数量
SHOW STATUS LIKE 'Slow_queries';

-- 查看表大小
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'metadata_db'
ORDER BY size_mb DESC;
```

## 性能目标

- 查询响应时间 < 100ms（简单查询）
- 查询响应时间 < 500ms（复杂查询）
- 连接池使用率 < 80%
- 慢查询数量 < 1%
- 数据库 CPU 使用率 < 70%

## 总结

数据库优化已完成：
- ✅ 所有必要的索引已创建
- ✅ HikariCP 连接池已配置
- ✅ JPA 查询已优化
- ✅ 事务管理已配置

建议定期监控数据库性能，根据实际情况调整配置参数。
