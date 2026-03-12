# Kiro Web Backend (Java)

数据仓库元数据管理系统后端服务 - Java/Spring Boot 实现

## 技术栈

- **Java**: 17+
- **Spring Boot**: 3.2.1
- **Spring Security**: JWT 认证
- **MyBatis-Plus**: 3.5.5
- **MySQL**: 8.0+
- **Redis**: 7.x
- **Elasticsearch**: 8.x
- **Maven**: 3.8+

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/kiro/metadata/
│   │       ├── MetadataApplication.java    # 主应用类
│   │       ├── config/                     # 配置类
│   │       ├── controller/                 # REST 控制器
│   │       ├── service/                    # 业务逻辑层
│   │       ├── repository/                 # 数据访问层
│   │       ├── entity/                     # 实体类
│   │       ├── dto/                        # 数据传输对象
│   │       ├── security/                   # 安全相关
│   │       ├── exception/                  # 异常处理
│   │       └── util/                       # 工具类
│   └── resources/
│       ├── application.yml                 # 主配置文件
│       ├── application-dev.yml             # 开发环境配置
│       ├── application-prod.yml            # 生产环境配置
│       └── logback-spring.xml              # 日志配置
└── test/
    └── java/
        └── com/kiro/metadata/
            ├── unit/                       # 单元测试
            ├── property/                   # 基于属性的测试
            └── integration/                # 集成测试
```

## 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x
- Elasticsearch 8.x

### 安装依赖

```bash
mvn clean install
```

### 配置数据库

1. 创建数据库：

```sql
CREATE DATABASE kiro_metadata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `src/main/resources/application-dev.yml` 中的数据库连接信息

### 运行应用

开发环境：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

或使用 IDE 运行 `MetadataApplication.java`

### 访问应用

- API 文档 (Swagger): http://localhost:8080/api/swagger-ui.html
- API Docs (OpenAPI): http://localhost:8080/api/v3/api-docs
- 健康检查: http://localhost:8080/api/actuator/health

## 测试

运行所有测试：

```bash
mvn test
```

运行单元测试：

```bash
mvn test -Dtest=*Test
```

运行基于属性的测试：

```bash
mvn test -Dtest=*Properties
```

生成测试覆盖率报告：

```bash
mvn test jacoco:report
```

## 构建

构建生产版本：

```bash
mvn clean package -Dmaven.test.skip=true
```

生成的 JAR 文件位于 `target/kiro-web-backend-1.0.0-SNAPSHOT.jar`

## 部署

### 使用 JAR 文件

```bash
java -jar target/kiro-web-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### 使用 Docker

```bash
docker build -t kiro-web-backend .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod kiro-web-backend
```

## 环境变量

生产环境需要配置以下环境变量：

- `DB_HOST`: 数据库主机
- `DB_PORT`: 数据库端口
- `DB_NAME`: 数据库名称
- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码
- `REDIS_HOST`: Redis 主机
- `REDIS_PORT`: Redis 端口
- `REDIS_PASSWORD`: Redis 密码
- `ELASTICSEARCH_URIS`: Elasticsearch 地址
- `JWT_SECRET`: JWT 密钥（必须修改）
- `CORS_ALLOWED_ORIGINS`: 允许的跨域源

## 开发指南

### 代码规范

- 遵循 Java 代码规范
- 使用 Lombok 减少样板代码
- 所有 API 必须有 Swagger 注解
- 所有公共方法必须有 JavaDoc 注释

### 提交规范

- feat: 新功能
- fix: 修复 bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建/工具相关

## 许可证

MIT License
