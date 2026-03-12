# Spring Security 和 JWT 快速开始指南

## 概述

本指南帮助你快速了解和使用系统的 Spring Security 和 JWT 认证配置。

## 已完成的配置

### 1. SecurityConfig 配置类

✅ **位置**: `src/main/java/com/kiro/metadata/config/SecurityConfig.java`

**功能**:
- BCrypt 密码加密器
- 安全过滤器链（禁用 CSRF、配置 CORS、白名单路径）
- 无状态会话管理
- CORS 跨域配置

### 2. JWT 配置属性

✅ **位置**: `src/main/java/com/kiro/metadata/config/JwtProperties.java`

**绑定配置**: `application.yml` 中的 `jwt.*` 配置

**属性**:
- `secret`: JWT 密钥
- `expiration`: Access Token 过期时间
- `refreshExpiration`: Refresh Token 过期时间
- `issuer`: Token 签发者
- `audience`: Token 受众

### 3. CORS 配置属性

✅ **位置**: `src/main/java/com/kiro/metadata/config/CorsProperties.java`

**绑定配置**: `application.yml` 中的 `app.cors.*` 配置

**属性**:
- `allowedOrigins`: 允许的源（域名）
- `allowedMethods`: 允许的 HTTP 方法
- `allowedHeaders`: 允许的请求头
- `allowCredentials`: 是否允许携带凭证

### 4. 配置文件

✅ **开发环境**: `src/main/resources/application-dev.yml`
✅ **生产环境**: `src/main/resources/application-prod.yml`

## 配置示例

### 开发环境配置 (application-dev.yml)

```yaml
# JWT Configuration
jwt:
  secret: kiro-metadata-secret-key-change-this-in-production-environment-use-strong-random-key
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
  issuer: kiro-metadata-system
  audience: kiro-metadata-users

# CORS Configuration
app:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:5173
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
```

### 生产环境配置 (application-prod.yml)

```yaml
# JWT Configuration
jwt:
  secret: ${JWT_SECRET}  # 必须通过环境变量设置
  expiration: ${JWT_EXPIRATION:3600000}  # 1 hour
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}
  issuer: kiro-metadata-system
  audience: kiro-metadata-users

# CORS Configuration
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
```

## 白名单路径

以下路径无需认证即可访问：

| 路径 | 说明 |
|------|------|
| `/api/v1/auth/**` | 认证端点（登录、注册、刷新令牌） |
| `/swagger-ui/**` | Swagger UI 界面 |
| `/v3/api-docs/**` | OpenAPI 文档 |
| `/actuator/health` | 健康检查端点 |
| `/actuator/info` | 应用信息端点 |

## 使用 BCrypt 密码加密

```java
@Autowired
private PasswordEncoder passwordEncoder;

// 加密密码
String rawPassword = "password123";
String encodedPassword = passwordEncoder.encode(rawPassword);

// 验证密码
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

## 后续实现任务

以下功能将在后续任务中实现：

### Task 4.1: JWT 工具类
- [ ] 创建 `JwtTokenProvider` 类
- [ ] 实现 `generateAccessToken()` 方法
- [ ] 实现 `generateRefreshToken()` 方法
- [ ] 实现 `validateToken()` 方法
- [ ] 实现 `getUsernameFromToken()` 方法

### Task 4.2: JWT 认证过滤器
- [ ] 创建 `JwtAuthenticationFilter` 类
- [ ] 实现 `doFilterInternal()` 方法
- [ ] 从 Authorization header 提取 Token
- [ ] 验证 Token 并设置 SecurityContext

### Task 4.3: UserDetailsService
- [ ] 创建 `UserDetailsServiceImpl` 类
- [ ] 实现 `loadUserByUsername()` 方法
- [ ] 从数据库加载用户信息

### Task 4.4: 认证服务
- [ ] 创建 `AuthService` 类
- [ ] 实现 `login()` 方法
- [ ] 实现 `logout()` 方法（Token 黑名单）
- [ ] 实现 `refreshToken()` 方法

### Task 4.6: 权限验证
- [ ] 实现基于角色的访问控制（RBAC）
- [ ] 创建自定义注解 `@RequireRole`
- [ ] 创建 AOP 切面验证权限

## 测试

### 测试 BCrypt 密码加密

```java
@SpringBootTest
class SecurityConfigTest {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    void testPasswordEncoder() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 验证密码匹配
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        
        // 验证密码已加密
        assertNotEquals(rawPassword, encodedPassword);
        
        // 验证每次加密结果不同（因为盐值不同）
        String encodedPassword2 = passwordEncoder.encode(rawPassword);
        assertNotEquals(encodedPassword, encodedPassword2);
    }
}
```

### 测试白名单访问

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testHealthEndpointAccess() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/health", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void testProtectedEndpointWithoutAuth() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/tables", String.class);
        
        // 应该返回 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
```

## 生产环境部署

### 1. 生成强随机密钥

```bash
# 使用 OpenSSL 生成 256 位随机密钥
openssl rand -base64 32

# 或使用 UUID
uuidgen | tr -d '-'
```

### 2. 设置环境变量

```bash
# JWT 密钥（必须）
export JWT_SECRET="your-strong-random-secret-key-at-least-256-bits"

# JWT 过期时间（可选，默认 1 小时）
export JWT_EXPIRATION=3600000

# CORS 允许的源（必须）
export CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
```

### 3. Docker 部署

在 `docker-compose.yml` 中设置环境变量：

```yaml
services:
  backend:
    image: kiro-web-backend:latest
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION=3600000
      - CORS_ALLOWED_ORIGINS=https://yourdomain.com
      - SPRING_PROFILES_ACTIVE=prod
```

## 常见问题

### Q: 为什么禁用了 CSRF？
A: 因为使用 JWT 进行无状态认证，不使用 Session 和 Cookie，所以不需要 CSRF 保护。

### Q: 如何修改 Token 过期时间？
A: 修改 `application.yml` 中的 `jwt.expiration` 配置，单位为毫秒。

### Q: 如何添加新的白名单路径？
A: 在 `SecurityConfig.java` 的 `WHITE_LIST` 数组中添加新路径。

### Q: 生产环境必须修改什么配置？
A: 必须修改：
1. JWT 密钥（`JWT_SECRET` 环境变量）
2. CORS 允许的源（`CORS_ALLOWED_ORIGINS` 环境变量）
3. 缩短 Token 过期时间（建议 1-2 小时）

## 参考文档

- [完整配置文档](../SECURITY_CONFIGURATION.md)
- [Spring Security 官方文档](https://docs.spring.io/spring-security/reference/index.html)
- [jjwt 官方文档](https://github.com/jwtk/jjwt)

## 更新日志

- **2024-01-XX**: 完成 Spring Security 和 JWT 基础配置（Task 1.5）
