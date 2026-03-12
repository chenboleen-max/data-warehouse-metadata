# Task 1.6: 异步任务和线程池配置

## 任务概述

为数据仓库元数据管理系统配置异步任务执行器和线程池，支持异步导出等耗时操作。

## 实现内容

### 1. AsyncProperties 配置属性类

**文件**: `src/main/java/com/kiro/metadata/config/AsyncProperties.java`

**功能**:
- 从 `application.yml` 读取 `app.async.*` 配置
- 绑定线程池参数到 Java 对象

**配置属性**:
- `corePoolSize`: 核心线程数（默认: 5）
- `maxPoolSize`: 最大线程数（默认: 10）
- `queueCapacity`: 队列容量（默认: 100）
- `threadNamePrefix`: 线程名称前缀（默认: "async-task-"）

### 2. AsyncConfig 配置类

**文件**: `src/main/java/com/kiro/metadata/config/AsyncConfig.java`

**功能**:
- 实现 `AsyncConfigurer` 接口
- 配置 `ThreadPoolTaskExecutor` Bean
- 配置异步异常处理器

**线程池配置**:
- 核心线程数: 从配置文件读取
- 最大线程数: 从配置文件读取
- 队列容量: 从配置文件读取
- 拒绝策略: `CallerRunsPolicy`（调用线程执行，避免任务丢失）
- 优雅关闭: 等待任务完成后再关闭（超时 60 秒）

**异常处理**:
- 捕获异步方法的未处理异常
- 记录详细的错误日志（方法名、参数、异常信息）

### 3. @EnableAsync 注解

**文件**: `src/main/java/com/kiro/metadata/MetadataApplication.java`

**状态**: ✅ 已存在

`@EnableAsync` 注解已在主应用类中配置，无需额外添加。

### 4. 配置文件

**开发环境** (`application-dev.yml`):
```yaml
app:
  async:
    core-pool-size: 5
    max-pool-size: 10
    queue-capacity: 100
    thread-name-prefix: async-task-
```

**生产环境** (`application-prod.yml`):
```yaml
app:
  async:
    core-pool-size: 10
    max-pool-size: 20
    queue-capacity: 200
    thread-name-prefix: async-task-
```

### 5. 单元测试

**文件**: `src/test/java/com/kiro/metadata/config/AsyncConfigTest.java`

**测试内容**:
- 线程池配置正确性验证
- 异步属性默认值验证
- 拒绝策略验证

## 线程池工作原理

1. **任务提交时**，如果线程数 < corePoolSize，创建新线程执行任务
2. **如果线程数 >= corePoolSize**，任务被放入队列
3. **如果队列已满且线程数 < maxPoolSize**，创建新线程执行任务
4. **如果队列已满且线程数 >= maxPoolSize**，执行拒绝策略（CallerRunsPolicy）

## 使用示例

### 在 Service 中使用异步方法

```java
@Service
@RequiredArgsConstructor
public class ImportExportService {
    
    /**
     * 异步导出 CSV
     * 使用 @Async 注解标记异步方法
     * 指定使用 taskExecutor 线程池
     */
    @Async("taskExecutor")
    public CompletableFuture<String> exportCsv(Map<String, Object> filters) {
        try {
            // 执行导出逻辑
            String filePath = performExport(filters);
            return CompletableFuture.completedFuture(filePath);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 异步发送通知
     * 返回类型为 void 的异步方法
     * 异常会被 AsyncUncaughtExceptionHandler 捕获
     */
    @Async("taskExecutor")
    public void sendNotification(String userId, String message) {
        // 发送通知逻辑
    }
}
```

### 调用异步方法

```java
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExportController {
    
    private final ImportExportService importExportService;
    
    @PostMapping("/csv")
    public ResponseEntity<ExportTaskResponse> exportCsv(@RequestBody ExportRequest request) {
        // 提交异步任务
        CompletableFuture<String> future = importExportService.exportCsv(request.getFilters());
        
        // 立即返回任务 ID
        String taskId = UUID.randomUUID().toString();
        
        // 异步任务完成后的处理
        future.thenAccept(filePath -> {
            // 更新任务状态
            updateTaskStatus(taskId, "COMPLETED", filePath);
        }).exceptionally(ex -> {
            // 处理异常
            updateTaskStatus(taskId, "FAILED", ex.getMessage());
            return null;
        });
        
        return ResponseEntity.ok(new ExportTaskResponse(taskId, "PENDING"));
    }
}
```

## 配置优势

1. **性能优化**: 异步执行耗时操作，不阻塞主线程
2. **资源控制**: 通过线程池限制并发数，避免系统过载
3. **优雅降级**: CallerRunsPolicy 拒绝策略，任务不会丢失
4. **可配置性**: 不同环境使用不同的线程池参数
5. **可观测性**: 线程名称前缀便于日志追踪和问题排查
6. **异常处理**: 统一的异常处理机制，记录详细日志

## 适用场景

- ✅ 异步导出大量数据（需求 12.4）
- ✅ 异步发送通知和邮件
- ✅ 异步更新数据质量指标
- ✅ 异步索引元数据到 Elasticsearch
- ✅ 其他耗时的后台任务

## 注意事项

1. **事务传播**: 异步方法在独立线程中执行，不会继承调用方的事务上下文
2. **异常处理**: 
   - 返回 `void` 的异步方法：异常由 `AsyncUncaughtExceptionHandler` 处理
   - 返回 `Future/CompletableFuture` 的异步方法：异常应在调用方处理
3. **代理限制**: `@Async` 注解只对通过 Spring 代理调用的方法生效，同类内部调用无效
4. **线程安全**: 确保异步方法中使用的对象是线程安全的

## 验证方法

### 1. 启动应用查看日志

```bash
mvn spring-boot:run
```

应该看到类似日志:
```
初始化异步任务线程池 - 核心线程数: 5, 最大线程数: 10, 队列容量: 100, 线程名称前缀: async-task-
异步任务线程池初始化完成
```

### 2. 运行单元测试

```bash
mvn test -Dtest=AsyncConfigTest
```

### 3. 监控线程池状态

可以通过 Spring Boot Actuator 监控线程池状态:

```java
@Autowired
private ThreadPoolTaskExecutor taskExecutor;

public ThreadPoolStats getThreadPoolStats() {
    ThreadPoolExecutor executor = taskExecutor.getThreadPoolExecutor();
    return ThreadPoolStats.builder()
        .activeCount(executor.getActiveCount())
        .poolSize(executor.getPoolSize())
        .queueSize(executor.getQueue().size())
        .completedTaskCount(executor.getCompletedTaskCount())
        .build();
}
```

## 相关文件

- `src/main/java/com/kiro/metadata/config/AsyncProperties.java` - 配置属性类
- `src/main/java/com/kiro/metadata/config/AsyncConfig.java` - 配置类
- `src/main/java/com/kiro/metadata/MetadataApplication.java` - 主应用类（@EnableAsync）
- `src/main/resources/application-dev.yml` - 开发环境配置
- `src/main/resources/application-prod.yml` - 生产环境配置
- `src/test/java/com/kiro/metadata/config/AsyncConfigTest.java` - 单元测试

## 任务完成状态

✅ 创建 AsyncProperties 配置属性类
✅ 创建 AsyncConfig 配置类
✅ 配置 ThreadPoolTaskExecutor Bean
✅ 配置线程池参数（核心线程数、最大线程数、队列容量、线程名称前缀）
✅ 配置拒绝策略（CallerRunsPolicy）
✅ 配置优雅关闭（等待任务完成）
✅ 配置异步异常处理器
✅ 验证 @EnableAsync 注解已存在
✅ 创建单元测试
✅ 添加详细的 JavaDoc 注释

## 下一步

任务 1.6 已完成。异步任务配置已就绪，可以在后续任务中使用 `@Async` 注解实现异步导出功能（任务 12.4）。

**注意**: 项目存在 Lombok 注解处理器配置问题，已在 `pom.xml` 中添加了 Lombok 注解处理器路径配置，但仍需解决 Java/Lombok 版本兼容性问题。建议检查 Java 版本和 Lombok 版本的兼容性。
