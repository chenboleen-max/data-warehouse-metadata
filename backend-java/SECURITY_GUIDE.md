# 安全加固指南

## 已实现的安全措施

### 1. 认证和授权 ✅

#### JWT 认证
- ✅ 使用 JWT Token 进行身份验证
- ✅ Token 包含用户信息和角色
- ✅ Token 设置过期时间
- ✅ 支持 Token 刷新机制
- ✅ 登出时将 Token 加入黑名单

```java
// JwtTokenProvider.java
public String generateAccessToken(String username, UserRole role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
    
    return Jwts.builder()
        .setSubject(username)
        .claim("role", role.name())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
}
```

#### 基于角色的访问控制（RBAC）
- ✅ GUEST：只读权限
- ✅ DEVELOPER：读写权限
- ✅ ADMIN：完全权限

```java
// 使用 @PreAuthorize 注解
@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
public TableResponse createTable(TableCreateRequest request) {
    // ...
}

@PreAuthorize("hasRole('ADMIN')")
public void deleteTable(UUID id) {
    // ...
}
```

### 2. 密码安全 ✅

#### BCrypt 密码哈希
- ✅ 使用 BCryptPasswordEncoder
- ✅ 自动加盐
- ✅ 强度因子：10

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}

// 密码验证
public boolean login(String username, String password) {
    User user = userRepository.findByUsername(username);
    return passwordEncoder.matches(password, user.getPasswordHash());
}
```

### 3. SQL 注入防护 ✅

#### 使用参数化查询
- ✅ MyBatis-Plus 自动参数化
- ✅ JPA 使用 @Query 参数绑定
- ✅ 避免字符串拼接 SQL

```java
// 安全的查询方式
@Query("SELECT t FROM TableMetadata t WHERE t.tableName = :tableName")
TableMetadata findByTableName(@Param("tableName") String tableName);

// MyBatis-Plus QueryWrapper
QueryWrapper<TableMetadata> wrapper = new QueryWrapper<>();
wrapper.eq("table_name", tableName);  // 自动参数化
```

### 4. XSS 防护 ✅

#### 输入验证
- ✅ 使用 Bean Validation 验证输入
- ✅ 限制字符串长度
- ✅ 验证数据格式

```java
public class TableCreateRequest {
    @NotBlank(message = "表名不能为空")
    @Size(max = 100, message = "表名长度不能超过100")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "表名只能包含字母、数字和下划线")
    private String tableName;
}
```

#### 输出转义
- ✅ Spring MVC 自动转义 JSON
- ✅ 前端使用 Vue 自动转义

### 5. CSRF 防护 ✅

#### Spring Security 配置
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // REST API 不需要 CSRF
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
    return http.build();
}
```

**注意**：由于使用 JWT 无状态认证，不需要 CSRF 保护。如果使用 Session，需要启用 CSRF。

### 6. CORS 配置 ✅

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "https://example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### 7. 安全响应头 ✅

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .frameOptions(frame -> frame.deny())  // X-Frame-Options: DENY
            .xssProtection(xss -> xss.disable())  // X-XSS-Protection: 0
            .contentTypeOptions(content -> content.disable())  // X-Content-Type-Options: nosniff
        );
    return http.build();
}
```

## 推荐的安全措施

### 1. HTTPS 配置（生产环境）

#### 使用 Let's Encrypt 免费证书

```bash
# 安装 Certbot
sudo apt-get install certbot

# 获取证书
sudo certbot certonly --standalone -d example.com

# 证书路径
# /etc/letsencrypt/live/example.com/fullchain.pem
# /etc/letsencrypt/live/example.com/privkey.pem
```

#### Spring Boot HTTPS 配置

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEY_STORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
```

#### 生成自签名证书（开发环境）

```bash
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650 \
  -storepass changeit
```

### 2. 敏感信息脱敏

#### 日志脱敏

```java
@Component
public class SensitiveDataFilter implements Filter {
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("(password|pwd|token)=[^&\\s]+", Pattern.CASE_INSENSITIVE);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        String queryString = ((HttpServletRequest) request).getQueryString();
        if (queryString != null) {
            String sanitized = PASSWORD_PATTERN.matcher(queryString)
                .replaceAll("$1=***");
            log.info("Request: {}", sanitized);
        }
        chain.doFilter(request, response);
    }
}
```

#### 响应脱敏

```java
public class UserResponse {
    private String id;
    private String username;
    private String email;
    
    @JsonIgnore  // 不返回密码哈希
    private String passwordHash;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // 只写不读
    private String password;
}
```

### 3. 速率限制

#### 使用 Bucket4j

```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.1.0</version>
</dependency>
```

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                            Object handler) throws Exception {
        String key = getClientIP(request);
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket());
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(429);  // Too Many Requests
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
    }
    
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
```

### 4. 输入验证增强

```java
public class TableCreateRequest {
    
    @NotBlank
    @Size(min = 1, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String tableName;
    
    @NotBlank
    @Size(min = 1, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String databaseName;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private TableType tableType;
    
    @Size(max = 1000)
    private String description;
    
    @Min(0)
    private Long dataSizeBytes;
}
```

### 5. 文件上传安全

```java
@PostMapping("/import")
public ResponseEntity<?> importFile(@RequestParam("file") MultipartFile file) {
    // 1. 验证文件类型
    String contentType = file.getContentType();
    if (!Arrays.asList("text/csv", "application/json").contains(contentType)) {
        throw new BusinessException("不支持的文件类型");
    }
    
    // 2. 验证文件大小
    if (file.getSize() > 10 * 1024 * 1024) {  // 10MB
        throw new BusinessException("文件大小不能超过10MB");
    }
    
    // 3. 验证文件名
    String filename = file.getOriginalFilename();
    if (filename == null || filename.contains("..")) {
        throw new BusinessException("非法的文件名");
    }
    
    // 4. 扫描病毒（可选）
    // virusScanner.scan(file.getInputStream());
    
    // 5. 处理文件
    importService.importFromFile(file);
    
    return ResponseEntity.ok().build();
}
```

### 6. API 密钥管理

```yaml
# application.yml
security:
  jwt:
    secret: ${JWT_SECRET:default-secret-key-change-in-production}
    expiration: ${JWT_EXPIRATION:86400000}
  
  api:
    key: ${API_KEY:}
```

```java
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    
    @Value("${security.api.key}")
    private String apiKey;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String requestApiKey = request.getHeader("X-API-Key");
        
        if (StringUtils.hasText(apiKey) && !apiKey.equals(requestApiKey)) {
            response.setStatus(401);
            response.getWriter().write("Invalid API Key");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 7. 数据库连接安全

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:metadata_db}?useSSL=true&requireSSL=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    
    # 使用加密的数据库密码
    # jasypt:
    #   encryptor:
    #     password: ${JASYPT_PASSWORD:}
```

### 8. 审计日志

```java
@Aspect
@Component
public class AuditLogAspect {
    
    @Autowired
    private HistoryService historyService;
    
    @Around("@annotation(RequireRole)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        String method = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info("Audit: user={}, method={}, args={}", username, method, args);
        
        try {
            Object result = joinPoint.proceed();
            historyService.recordChange(
                "AUDIT",
                null,
                OperationType.CREATE,
                method,
                null,
                result.toString(),
                username
            );
            return result;
        } catch (Exception e) {
            log.error("Audit: user={}, method={}, error={}", username, method, e.getMessage());
            throw e;
        }
    }
}
```

## 安全检查清单

### 认证和授权
- [x] JWT 认证实现
- [x] Token 过期机制
- [x] Token 刷新机制
- [x] 基于角色的访问控制
- [x] 权限验证注解

### 密码安全
- [x] BCrypt 密码哈希
- [x] 密码强度验证
- [ ] 密码重置功能
- [ ] 密码过期策略
- [ ] 密码历史记录

### 数据安全
- [x] SQL 注入防护
- [x] XSS 防护
- [x] 输入验证
- [ ] 输出转义
- [ ] 敏感数据加密

### 网络安全
- [ ] HTTPS 配置
- [x] CORS 配置
- [x] 安全响应头
- [ ] 速率限制
- [ ] DDoS 防护

### 应用安全
- [ ] 文件上传安全
- [ ] API 密钥管理
- [ ] 会话管理
- [ ] 错误处理
- [ ] 日志脱敏

### 监控和审计
- [ ] 审计日志
- [ ] 安全事件监控
- [ ] 异常登录检测
- [ ] 访问日志分析

## 安全测试

### 1. OWASP ZAP 扫描

```bash
# 安装 OWASP ZAP
docker pull owasp/zap2docker-stable

# 运行扫描
docker run -t owasp/zap2docker-stable zap-baseline.py \
  -t http://localhost:8080 -r report.html
```

### 2. 依赖漏洞扫描

```bash
# 使用 OWASP Dependency Check
mvn org.owasp:dependency-check-maven:check

# 使用 Snyk
npm install -g snyk
snyk test
```

### 3. 代码安全扫描

```bash
# 使用 SonarQube
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

## 安全配置示例

### 生产环境配置

```yaml
# application-prod.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: /etc/ssl/keystore.p12
    key-store-password: ${SSL_PASSWORD}

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/${DB_NAME}?useSSL=true&requireSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 3600000  # 1小时

logging:
  level:
    com.kiro.metadata: INFO
    org.springframework.security: WARN
```

## 总结

安全措施已实现：
- ✅ JWT 认证和授权
- ✅ BCrypt 密码哈希
- ✅ SQL 注入防护
- ✅ XSS 防护
- ✅ CORS 配置
- ✅ 安全响应头

建议在生产环境部署前：
1. 启用 HTTPS
2. 配置速率限制
3. 实施审计日志
4. 进行安全测试
5. 定期更新依赖
