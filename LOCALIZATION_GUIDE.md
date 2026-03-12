# 系统中文化指南

本文档说明如何将系统中的英文注释和提示信息改为中文。

## 重要提示

**⚠️ 警告：不要使用自动翻译脚本！**

自动翻译脚本可能会破坏 Java 代码结构，包括：
- 将 `import` 关键字翻译成"导入"
- 将包名中的英文单词翻译（如 `cache` → "缓存"）
- 破坏代码语法

**推荐方法：手动翻译或使用 IDE 的查找替换功能，仅翻译注释和字符串字面量。**

## 已完成的中文化工作

### 1. 国际化配置文件 ✅

已创建 `backend-java/src/main/resources/messages_zh_CN.properties`，包含：
- 认证相关消息（登录、登出、令牌刷新）
- 权限相关消息
- 表元数据相关消息
- 字段元数据相关消息
- 血缘关系相关消息
- 数据目录相关消息
- 数据质量相关消息
- 变更历史相关消息
- 搜索相关消息
- 导入导出相关消息
- 验证相关消息
- 系统错误消息
- 数据库错误消息
- 缓存相关消息

### 2. 前端界面 ✅

前端所有页面已使用中文：
- 登录页面
- 表元数据管理页面
- 血缘关系可视化页面
- 搜索页面
- 数据目录页面
- 数据质量页面
- 变更历史页面
- 导入导出页面

## 需要中文化的内容

### 后端 Java 代码

#### 1. 注释中文化

**位置**: `backend-java/src/main/java/**/*.java`

**需要修改的内容**:
- 类注释
- 方法注释
- 行内注释

**示例**:

```java
// 修改前
/**
 * User login
 * 
 * Validates username and password, generates JWT tokens
 */
public TokenResponse login(LoginRequest loginRequest) {
    // Find user by username
    User user = userRepository.findByUsername(username);
}

// 修改后
/**
 * 用户登录
 * 
 * 验证用户名和密码，生成 JWT 令牌
 */
public TokenResponse login(LoginRequest loginRequest) {
    // 根据用户名查找用户
    User user = userRepository.findByUsername(username);
}
```

#### 2. 异常消息中文化

**位置**: 所有抛出异常的地方

**示例**:

```java
// 修改前
throw new UsernameNotFoundException("User not found: " + username);
throw new BadCredentialsException("Invalid username or password");

// 修改后
throw new UsernameNotFoundException("用户不存在: " + username);
throw new BadCredentialsException("用户名或密码错误");
```

#### 3. 日志消息中文化

**位置**: 所有 `log.info()`, `log.warn()`, `log.error()` 等

**示例**:

```java
// 修改前
log.info("User login attempt: username={}", username);
log.warn("Login failed: user not found");
log.error("Database connection failed");

// 修改后
log.info("用户登录尝试: username={}", username);
log.warn("登录失败: 用户不存在");
log.error("数据库连接失败");
```

## 使用国际化配置

### 1. 在 Spring Boot 中启用国际化

**配置文件**: `application.yml`

```yaml
spring:
  messages:
    basename: messages
    encoding: UTF-8
    fallback-to-system-locale: false
    default-locale: zh_CN
```

### 2. 在代码中使用国际化消息

```java
@Autowired
private MessageSource messageSource;

public String getLocalizedMessage(String code, Object... args) {
    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
}

// 使用示例
String message = getLocalizedMessage("auth.login.failed.user_not_found");
throw new UsernameNotFoundException(message);
```

### 3. 创建 MessageService

```java
@Service
public class MessageService {
    
    @Autowired
    private MessageSource messageSource;
    
    public String getMessage(String code) {
        return getMessage(code, null);
    }
    
    public String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
```

## 批量翻译脚本

已创建 Python 脚本 `scripts/translate_comments.py` 用于批量翻译：

```bash
# 运行翻译脚本
cd kiro_web
python3 scripts/translate_comments.py
```

**脚本功能**:
- 自动扫描所有 Java 文件
- 替换常见的英文注释和消息
- 保持代码格式不变
- 显示翻译进度

## 手动翻译指南

### 优先级

1. **高优先级** - 用户可见的消息
   - 异常消息
   - API 响应消息
   - 验证错误消息

2. **中优先级** - 开发者可见的消息
   - 日志消息
   - 方法注释

3. **低优先级** - 内部注释
   - 行内注释
   - 变量说明

### 翻译原则

1. **准确性**: 确保翻译准确传达原意
2. **简洁性**: 使用简洁的中文表达
3. **一致性**: 相同的英文使用相同的中文翻译
4. **专业性**: 使用专业术语

### 常用术语对照表

| 英文 | 中文 |
|------|------|
| Authentication | 认证 |
| Authorization | 授权 |
| Token | 令牌 |
| User | 用户 |
| Password | 密码 |
| Login | 登录 |
| Logout | 登出 |
| Permission | 权限 |
| Role | 角色 |
| Table | 表 |
| Column | 字段 |
| Metadata | 元数据 |
| Lineage | 血缘关系 |
| Catalog | 目录 |
| Quality | 质量 |
| History | 历史 |
| Search | 搜索 |
| Import | 导入 |
| Export | 导出 |
| Cache | 缓存 |
| Database | 数据库 |
| Query | 查询 |
| Create | 创建 |
| Update | 更新 |
| Delete | 删除 |
| Validate | 验证 |
| Generate | 生成 |
| Process | 处理 |
| Request | 请求 |
| Response | 响应 |
| Error | 错误 |
| Success | 成功 |
| Failed | 失败 |

## 验证中文化

### 1. 编译检查

```bash
cd backend-java
mvn clean compile
```

### 2. 运行测试

```bash
mvn test
```

### 3. 启动应用验证

```bash
docker-compose up -d
```

访问应用，检查：
- 登录页面提示信息
- 错误消息
- 成功提示
- 表单验证消息

## 注意事项

1. **不要翻译**:
   - 代码中的变量名
   - 方法名
   - 类名
   - 包名
   - 配置键名

2. **保持格式**:
   - 保持原有的缩进
   - 保持原有的换行
   - 保持原有的标点符号位置

3. **测试验证**:
   - 翻译后必须编译通过
   - 翻译后必须测试通过
   - 翻译后功能正常

## 示例文件

### AuthService.java（部分已翻译）

```java
/**
 * 认证服务
 * 
 * 处理用户认证操作，包括：
 * - 登录（用户名/密码验证，JWT 生成）
 * - 登出（令牌黑名单管理）
 * - 令牌刷新
 * - 从 SecurityContext 获取当前用户
 */
@Service
public class AuthService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 令牌响应
     */
    public TokenResponse login(LoginRequest loginRequest) {
        log.info("用户登录尝试: username={}", loginRequest.getUsername());
        
        // 根据用户名查找用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        // 检查账号是否激活
        if (!user.getIsActive()) {
            throw new BadCredentialsException("账号已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        
        // 生成令牌
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        
        log.info("用户登录成功: username={}", user.getUsername());
        
        return new TokenResponse(accessToken);
    }
}
```

## 进度跟踪

### 后端文件

- [x] messages_zh_CN.properties（国际化配置）
- [ ] AuthService.java（部分完成）
- [ ] MetadataService.java
- [ ] ColumnService.java
- [ ] LineageService.java
- [ ] SearchService.java
- [ ] CatalogService.java
- [ ] QualityService.java
- [ ] HistoryService.java
- [ ] ImportExportService.java
- [ ] 所有 Controller 类
- [ ] 所有 Exception 类
- [ ] 所有 Config 类

### 前端文件

- [x] 所有 Vue 组件（已使用中文）
- [x] 所有提示消息（已使用中文）

## 总结

系统中文化是一个持续的过程，建议：

1. 优先翻译用户可见的消息
2. 使用国际化配置文件统一管理
3. 保持翻译的一致性
4. 定期检查和更新翻译

如有疑问，请参考本文档或联系开发团队。
