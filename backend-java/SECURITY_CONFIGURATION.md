# Spring Security 和 JWT 配置文档

## 概述

本文档描述了数据仓库元数据管理系统的 Spring Security 和 JWT 认证配置。系统使用 Spring Security 6.x 和 JWT（JSON Web Token）实现无状态的认证和授权机制。

## 技术栈

- **Spring Security**: 6.x
- **JWT 库**: jjwt 0.12.3 (io.jsonwebtoken)
- **密码加密**: BCrypt
- **会话管理**: 无状态（Stateless）

## 核心配置

### 1. SecurityConfig 配置类

位置：`src/main/java/com/kiro/metadata/config/SecurityConfig.java`

#### 主要功能

1. **密码加密器配置**
   - 使用 `BCryptPasswordEncoder` 进行密码哈希
   - BCrypt 是一种自适应哈希算法，具有内置的盐值和可配置的工作因子

2. **安全过滤器链配置**
   - 禁用 CSRF（使用 JWT 无状态认证，不需要 CSRF 保护）
   - 配置 CORS 跨域资源共享
   - 配置白名单路径（无需认证）
   - 配置无状态会话管理
   - 禁用默认的表单登录和 HTTP Basic 认证

3. **CORS 配置**
   - 允许指定的前端域名跨域访问
   - 支持所有常用 HTTP 方法
   - 允许携带认证凭证
   - 暴露自定义响应头

#### 白名单路径

以下路径无需认证即可访问：

```
/api/v1/auth/**              # 认证端点（登录、注册、刷新令牌）
/swagger-ui/**               # Swagger UI 界面
/v3/api-docs/**              # OpenAPI 文档
/actuator/health             # 健康检查端点
/actuator/info               # 应用信息端点
```

### 2. JWT 配置属性

位置：`src/main/resources/application-dev.yml` 和 `application-prod.yml`

#### 配置项说明

```yaml
jwt:
  # JWT 密钥（用于签名和验证 Token）
  secret: kiro-metadata-secret-key-change-this-in-production-environment-use-strong-random-key
  
  # Access Token 过期时间（毫秒）
  # 开发环境：24 小时 = 86400000 毫秒
  # 生产环境建议：1-2 小时 = 3600000-7200000 毫秒
  expiration: 86400000
  
  # Refresh Token 过期时间（毫秒）
  # 默认：7 天 = 604800000 毫秒
  refresh-expiration: 604800000
  
  # Token 签发者
  issuer: kiro-metadata-system
  
  # Token 受众
  audience: kiro-metadata-users
```

#### 生产环境配置

生产环境必须通过环境变量设置 JWT 密钥：

```bash
export JWT_SECRET="your-strong-random-secret-key-at-least-256-bits"
export JWT_EXPIRATION=3600000  # 1 小时
```

**密钥生成建议**：
```bash
# 使用 OpenSSL 生成 256 位随机密钥
openssl rand -base64 32

# 或使用 UUID
uuidgen | tr -d '-'
```

### 3. CORS 配置属性

位置：`src/main/resources/application-dev.yml` 和 `application-prod.yml`

#### 配置项说明

```yaml
app:
  cors:
    # 允许的源（域名）
    allowed-origins: http://localhost:3000,http://localhost:5173
    
    # 允许的 HTTP 方法
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    
    # 允许的请求头（* 表示所有）
    allowed-headers: "*"
    
    # 是否允许携带凭证
    allow-credentials: true
```

#### 生产环境配置

```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
```

通过环境变量设置：
```bash
export CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
```

## Maven 依赖

JWT 相关依赖已在 `pom.xml` 中配置：

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

## 安全最佳实践

### 1. 密码安全

- ✅ 使用 BCrypt 加密密码
- ✅ 不存储明文密码
- ✅ BCrypt 自动生成盐值
- ✅ 密码验证使用恒定时间比较

### 2. JWT 安全

- ✅ 使用强随机密钥（至少 256 位）
- ✅ 生产环境通过环境变量配置密钥
- ✅ 设置合理的 Token 过期时间
- ✅ 使用 HTTPS 传输 Token
- ✅ Token 存储在 HTTP-only Cookie 或 Authorization header

### 3. CORS 安全

- ✅ 明确指定允许的源（不使用 `*`）
- ✅ 仅允许必要的 HTTP 方法
- ✅ 生产环境限制为 HTTPS 域名

### 4. 会话管理

- ✅ 使用无状态会话（JWT）
- ✅ 禁用 Session
- ✅ Token 黑名单机制（登出时将 Token 加入 Redis 黑名单）

## 后续实现任务

以下功能将在后续任务中实现：

1. **JWT 工具类** (Task 4.1)
   - `JwtTokenProvider`: 生成和验证 JWT Token
   - 支持 Access Token 和 Refresh Token

2. **JWT 认证过滤器** (Task 4.2)
   - `JwtAuthenticationFilter`: 拦截请求并验证 Token
   - 从 Authorization header 提取 Token
   - 设置 SecurityContext

3. **UserDetailsService** (Task 4.3)
   - `UserDetailsServiceImpl`: 从数据库加载用户信息
   - 转换为 Spring Security 的 UserDetails

4. **认证服务** (Task 4.4)
   - `AuthService`: 登录、登出、刷新令牌
   - Token 黑名单管理（使用 Redis）

5. **权限验证** (Task 4.6)
   - 基于角色的访问控制（RBAC）
   - 自定义注解 `@RequireRole`
   - AOP 切面验证权限

## 测试

### 单元测试

```java
@SpringBootTest
class SecurityConfigTest {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    void testPasswordEncoder() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertNotEquals(rawPassword, encodedPassword);
    }
}
```

### 集成测试

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testWhiteListAccess() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/health", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void testProtectedEndpointWithoutAuth() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/tables", String.class);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
```

## 故障排查

### 常见问题

1. **CORS 错误**
   - 检查 `allowed-origins` 配置是否包含前端域名
   - 确认前端请求包含正确的 Origin header
   - 检查是否启用了 `allow-credentials`

2. **401 Unauthorized**
   - 检查 Token 是否过期
   - 验证 Authorization header 格式：`Bearer <token>`
   - 确认 JWT 密钥配置正确

3. **403 Forbidden**
   - 检查用户角色和权限
   - 验证 `@PreAuthorize` 注解配置
   - 确认用户已认证

## 参考资料

- [Spring Security 6.x 官方文档](https://docs.spring.io/spring-security/reference/index.html)
- [jjwt 官方文档](https://github.com/jwtk/jjwt)
- [JWT 最佳实践](https://tools.ietf.org/html/rfc8725)
- [OWASP 认证备忘单](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

## 更新日志

- **2024-01-XX**: 初始版本，完成 Spring Security 和 JWT 基础配置
