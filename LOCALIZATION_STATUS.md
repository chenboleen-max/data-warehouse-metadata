# 系统中文化状态报告

## 执行日期
2026-03-13

## 当前状态

### ✅ 已完成

1. **国际化配置文件**
   - 文件：`backend-java/src/main/resources/messages_zh_CN.properties`
   - 状态：100% 完成
   - 包含所有系统提示消息的中文翻译

2. **前端界面**
   - 所有 Vue 组件：100% 使用中文
   - 所有用户提示消息：100% 使用中文
   - 文件数：约 20+ 个 Vue 文件

3. **部分后端文件**
   - `AuthService.java`：100% 完成（手动翻译）
   - `JwtAuthenticationEntryPoint.java`：100% 完成（手动翻译）

### ⚠️ 需要完成

**后端 Java 代码注释和消息**（约 100 个文件）

需要翻译的内容：
- 类注释（`/** ... */`）
- 方法注释（`/** ... */`）
- 行内注释（`// ...`）
- 日志消息（`log.info()`, `log.warn()`, `log.error()`）
- 异常消息（`throw new ...Exception("...")`）

**重要文件优先级：**

1. **高优先级** - 用户可见的消息
   - `controller/` 包下的所有 Controller 类
   - `exception/GlobalExceptionHandler.java`
   - `security/JwtAccessDeniedHandler.java`

2. **中优先级** - 核心业务逻辑
   - `service/` 包下的所有 Service 类
   - `security/` 包下的其他类

3. **低优先级** - 配置和工具类
   - `config/` 包下的配置类
   - `repository/` 包下的 Repository 接口
   - `dto/` 包下的 DTO 类

## 自动翻译脚本问题

### ❌ 失败的尝试

创建了批量翻译脚本 `scripts/batch_translate.py`，但执行后导致编译错误：

**问题：**
- 脚本将 Java 关键字 `import` 翻译成了"导入"
- 脚本将包名中的英文单词翻译（如 `cache` → "缓存"）
- 破坏了 Java 代码的语法结构

**错误示例：**
```java
// 错误的翻译结果
导入 org.springframework.boot.SpringApplication;
导入 org.springframework.缓存.annotation.EnableCaching;
```

**正确的应该是：**
```java
// 只翻译注释，不翻译代码
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 元数据管理应用主类
 */
```

### 🔧 解决方案

**不使用自动翻译脚本**，改为以下方法：

1. **手动翻译**（推荐）
   - 使用 IDE 逐个文件翻译
   - 只翻译注释和字符串字面量
   - 保持代码结构不变

2. **使用 IDE 的查找替换功能**
   - 仅在注释块内查找替换
   - 使用正则表达式精确匹配
   - 每次替换前预览结果

3. **使用国际化配置**
   - 将所有用户可见的消息移到 `messages_zh_CN.properties`
   - 在代码中使用 `MessageSource` 获取消息
   - 这样可以避免硬编码中文字符串

## 推荐的翻译流程

### 步骤 1：优先使用国际化配置

对于用户可见的消息，使用 `messages_zh_CN.properties`：

```java
// 不推荐：硬编码中文
throw new ResourceNotFoundException("用户不存在");

// 推荐：使用国际化配置
String message = messageSource.getMessage("auth.login.failed.user_not_found", null, locale);
throw new ResourceNotFoundException(message);
```

### 步骤 2：手动翻译注释

按优先级逐个文件翻译：

1. 打开文件
2. 翻译类注释
3. 翻译方法注释
4. 翻译行内注释
5. 验证编译通过
6. 提交更改

### 步骤 3：翻译日志消息

日志消息可以直接使用中文：

```java
// 可以接受
log.info("用户登录尝试: username={}", username);
log.warn("登录失败: 用户不存在 - {}", username);
log.error("数据库连接失败: {}", e.getMessage());
```

## 翻译原则

### ✅ 应该翻译

- 类和方法的 Javadoc 注释
- 行内注释（`// ...`）
- 日志消息字符串
- 异常消息字符串
- 用户提示消息

### ❌ 不应该翻译

- Java 关键字（`import`, `class`, `public`, `void` 等）
- 包名（`com.kiro.metadata`）
- 类名（`AuthService`, `UserRepository`）
- 方法名（`login`, `logout`, `findById`）
- 变量名（`username`, `password`, `token`）
- 注解（`@Service`, `@Controller`, `@Autowired`）
- 配置键名（`spring.datasource.url`）

## 常用术语对照表

| 英文 | 中文 | 使用场景 |
|------|------|----------|
| Authentication | 认证 | 用户身份验证 |
| Authorization | 授权 | 权限控制 |
| Token | 令牌 | JWT 令牌 |
| User | 用户 | 用户实体 |
| Password | 密码 | 用户密码 |
| Login | 登录 | 用户登录 |
| Logout | 登出 | 用户登出 |
| Permission | 权限 | 访问权限 |
| Role | 角色 | 用户角色 |
| Table | 表 | 数据库表 |
| Column | 字段 | 表字段 |
| Metadata | 元数据 | 元数据信息 |
| Lineage | 血缘关系 | 数据血缘 |
| Catalog | 目录 | 数据目录 |
| Quality | 质量 | 数据质量 |
| History | 历史 | 变更历史 |
| Search | 搜索 | 搜索功能 |
| Import | 导入 | 数据导入 |
| Export | 导出 | 数据导出 |
| Cache | 缓存 | Redis 缓存 |
| Database | 数据库 | 数据库 |
| Query | 查询 | 数据查询 |
| Create | 创建 | 创建操作 |
| Update | 更新 | 更新操作 |
| Delete | 删除 | 删除操作 |
| Validate | 验证 | 数据验证 |
| Generate | 生成 | 生成操作 |
| Process | 处理 | 数据处理 |
| Request | 请求 | HTTP 请求 |
| Response | 响应 | HTTP 响应 |
| Error | 错误 | 错误信息 |
| Success | 成功 | 成功消息 |
| Failed | 失败 | 失败消息 |

## 下一步行动

### 立即行动

1. **恢复被破坏的文件**
   - 由于没有 Git 仓库，需要手动恢复
   - 或者从备份中恢复

2. **删除错误的翻译脚本**
   - 删除或标记 `scripts/batch_translate.py` 为不可用

### 后续计划

1. **手动翻译高优先级文件**
   - Controller 类（约 8 个文件）
   - 异常处理类（约 5 个文件）

2. **手动翻译中优先级文件**
   - Service 类（约 10 个文件）
   - Security 类（约 7 个文件）

3. **手动翻译低优先级文件**
   - Config 类（约 10 个文件）
   - 其他工具类

### 预估工作量

- 高优先级：约 2-3 小时
- 中优先级：约 3-4 小时
- 低优先级：约 2-3 小时
- **总计：约 7-10 小时**

## 验证清单

完成翻译后，需要验证：

- [ ] 所有文件编译通过（`mvn clean compile`）
- [ ] 所有测试通过（`mvn test`）
- [ ] 应用可以正常启动
- [ ] API 返回的错误消息是中文
- [ ] 日志输出是中文
- [ ] 前端显示的消息是中文

## 总结

**当前进度：**
- 前端：100% 完成 ✅
- 国际化配置：100% 完成 ✅
- 后端代码：约 5% 完成 ⚠️

**主要问题：**
- 自动翻译脚本破坏了代码结构 ❌
- 需要手动恢复和翻译 ⚠️

**推荐方案：**
- 不使用自动脚本
- 手动逐个文件翻译
- 优先翻译用户可见的消息
- 使用国际化配置管理消息

**预计完成时间：**
- 如果全职工作：1-2 天
- 如果兼职工作：3-5 天
