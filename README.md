# 数据仓库元数据管理系统

一个功能完整的企业级数据仓库元数据管理系统，支持表元数据管理、血缘关系追踪、数据质量监控等功能。

## 功能特性

### 核心功能
- 📊 **表元数据管理** - 管理数据库表的元数据信息
- 🔗 **血缘关系追踪** - 可视化数据血缘关系图谱
- 🔍 **全文搜索** - 快速搜索表和字段
- 📁 **数据目录** - 分层组织数据资产
- 📈 **数据质量监控** - 监控数据质量指标
- 📝 **变更历史** - 追踪元数据变更记录
- 📤 **导入导出** - 支持 CSV、JSON、Hive Metastore

### 技术特性
- 🔐 **JWT 认证** - 安全的身份验证
- 👥 **角色权限** - 基于角色的访问控制
- 💾 **Redis 缓存** - 提升查询性能
- 🔎 **Elasticsearch** - 强大的全文搜索
- 🐳 **Docker 部署** - 一键部署所有服务
- 📊 **API 文档** - Swagger UI 交互式文档

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2+
- MyBatis-Plus 3.5+
- Spring Security 6.x
- MySQL 8.0+
- Redis 7.x
- Elasticsearch 8.x

### 前端
- Vue.js 3
- TypeScript
- Element Plus
- Vite
- D3.js / ECharts
- Pinia

## 快速开始

### 前置要求
- Docker 20.10+
- Docker Compose 2.0+

### 安装步骤

1. **克隆代码**
```bash
git clone <repository-url>
cd kiro_web
```

2. **配置环境变量**
```bash
cp .env.example .env
# 编辑 .env 文件，修改密码和密钥
```

3. **启动服务**
```bash
docker-compose up -d
```

4. **访问应用**
- 前端: http://localhost
- 后端 API: http://localhost:8080
- API 文档: http://localhost:8080/api/swagger-ui.html

5. **默认账号**
- 用户名: `admin`
- 密码: `admin123`

详细部署说明请参考 [DEPLOYMENT.md](DEPLOYMENT.md)

## 项目结构

```
kiro_web/
├── backend-java/          # 后端 Java 项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/              # 前端 Vue 项目
│   ├── src/
│   │   ├── api/          # API 客户端
│   │   ├── components/   # 组件
│   │   ├── stores/       # 状态管理
│   │   ├── types/        # 类型定义
│   │   └── views/        # 页面
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docker-compose.yml     # Docker Compose 配置
├── .env.example          # 环境变量模板
├── DEPLOYMENT.md         # 部署文档
└── README.md             # 本文件
```

## 开发指南

### 后端开发

```bash
cd backend-java

# 安装依赖
mvn clean install

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 文档

- [部署指南](DEPLOYMENT.md)
- [缓存优化](backend-java/CACHE_OPTIMIZATION.md)
- [数据库优化](backend-java/DATABASE_OPTIMIZATION.md)
- [性能优化](frontend/PERFORMANCE_OPTIMIZATION.md)
- [安全指南](backend-java/SECURITY_GUIDE.md)
- [测试清单](frontend/TESTING_CHECKLIST.md)

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
