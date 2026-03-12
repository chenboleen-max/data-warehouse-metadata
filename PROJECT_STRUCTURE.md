# 项目结构说明

## 完整目录树

```
kiro_web/
├── backend/                          # FastAPI 后端
│   ├── app/                         # 应用代码
│   │   ├── api/                     # API 路由
│   │   │   ├── routes/             # 路由模块
│   │   │   │   └── __init__.py
│   │   │   └── __init__.py
│   │   ├── core/                    # 核心配置
│   │   │   ├── config.py           # 应用配置（Settings）
│   │   │   └── __init__.py
│   │   ├── models/                  # SQLAlchemy 数据库模型
│   │   │   └── __init__.py
│   │   ├── schemas/                 # Pydantic 数据模式
│   │   │   └── __init__.py
│   │   ├── services/                # 业务逻辑服务
│   │   │   └── __init__.py
│   │   ├── __init__.py
│   │   └── main.py                  # FastAPI 应用入口
│   ├── tests/                       # 测试目录
│   │   └── __init__.py
│   ├── .env.example                 # 环境变量模板
│   ├── Dockerfile                   # Docker 镜像配置
│   ├── README.md                    # 后端文档
│   ├── requirements.txt             # Python 依赖
│   └── pyproject.toml              # Poetry 配置
│
├── frontend/                        # Vue.js 前端
│   ├── docker/                     # Docker 配置
│   │   └── nginx.conf              # Nginx 配置
│   ├── public/                     # 公共资源
│   ├── src/                        # 源代码
│   │   ├── api/                    # API 客户端
│   │   │   └── client.ts           # Axios 配置
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # Vue 组件
│   │   ├── router/                 # 路由配置
│   │   │   └── index.ts
│   │   ├── stores/                 # Pinia 状态管理
│   │   │   └── auth.ts             # 认证状态
│   │   ├── types/                  # TypeScript 类型
│   │   │   └── index.ts
│   │   ├── utils/                  # 工具函数
│   │   ├── views/                  # 页面视图
│   │   │   └── Home.vue            # 首页
│   │   ├── App.vue                 # 根组件
│   │   ├── main.ts                 # 应用入口
│   │   └── vite-env.d.ts          # Vite 类型定义
│   ├── .env.example                # 环境变量模板
│   ├── Dockerfile                  # Docker 镜像配置
│   ├── index.html                  # HTML 模板
│   ├── package.json                # Node 依赖
│   ├── README.md                   # 前端文档
│   ├── tsconfig.json              # TypeScript 配置
│   ├── tsconfig.node.json         # Node TypeScript 配置
│   └── vite.config.ts             # Vite 配置
│
├── docker/                         # Docker 配置
│   ├── mysql/                     # MySQL 配置
│   │   └── init.sql               # 初始化脚本
│   └── nginx/                     # Nginx 配置
│       ├── nginx.conf             # 主配置
│       └── conf.d/                # 站点配置
│           └── default.conf       # 默认站点
│
├── .env.example                   # 环境变量模板
├── .gitignore                     # Git 忽略文件
├── docker-compose.yml             # Docker Compose 配置
├── Makefile                       # 便捷命令
├── PROJECT_STRUCTURE.md           # 本文件
├── QUICKSTART.md                  # 快速开始指南
└── README.md                      # 项目主文档
```

## 核心文件说明

### 后端核心文件

| 文件 | 说明 |
|------|------|
| `backend/app/main.py` | FastAPI 应用入口，配置 CORS、路由等 |
| `backend/app/core/config.py` | 应用配置，使用 Pydantic Settings |
| `backend/requirements.txt` | Python 依赖列表 |
| `backend/pyproject.toml` | Poetry 配置，包含测试、格式化工具配置 |
| `backend/Dockerfile` | 多阶段构建的 Docker 镜像 |
| `backend/.env.example` | 环境变量模板 |

### 前端核心文件

| 文件 | 说明 |
|------|------|
| `frontend/src/main.ts` | Vue 应用入口，配置 Pinia、Router、Element Plus |
| `frontend/src/App.vue` | 根组件 |
| `frontend/src/router/index.ts` | 路由配置 |
| `frontend/src/api/client.ts` | Axios HTTP 客户端配置 |
| `frontend/src/stores/auth.ts` | 认证状态管理 |
| `frontend/vite.config.ts` | Vite 构建配置 |
| `frontend/package.json` | Node 依赖和脚本 |
| `frontend/Dockerfile` | 多阶段构建的 Docker 镜像 |

### Docker 配置文件

| 文件 | 说明 |
|------|------|
| `docker-compose.yml` | 定义所有服务（MySQL、Redis、ES、后端、前端、Nginx） |
| `docker/nginx/nginx.conf` | Nginx 主配置 |
| `docker/nginx/conf.d/default.conf` | 反向代理配置 |
| `docker/mysql/init.sql` | MySQL 初始化脚本 |
| `.env.example` | Docker Compose 环境变量模板 |

### 文档文件

| 文件 | 说明 |
|------|------|
| `README.md` | 项目主文档 |
| `QUICKSTART.md` | 快速开始指南 |
| `PROJECT_STRUCTURE.md` | 项目结构说明（本文件） |
| `backend/README.md` | 后端开发文档 |
| `frontend/README.md` | 前端开发文档 |

### 工具文件

| 文件 | 说明 |
|------|------|
| `Makefile` | 便捷命令集合 |
| `.gitignore` | Git 忽略规则 |

## 技术栈总结

### 后端
- **语言**: Python 3.9+
- **框架**: FastAPI
- **ORM**: SQLAlchemy 2.0
- **数据库驱动**: PyMySQL（纯 Python，无需编译）
- **数据验证**: Pydantic
- **认证**: JWT (python-jose)
- **测试**: pytest, hypothesis

### 前端
- **语言**: TypeScript
- **框架**: Vue.js 3 (Composition API)
- **UI 库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **构建工具**: Vite
- **HTTP 客户端**: Axios
- **图表**: ECharts, D3.js

### 数据存储
- **主数据库**: MySQL 8.0+ (utf8mb4)
- **缓存**: Redis 7.x
- **搜索引擎**: Elasticsearch 8.x

### 部署
- **容器化**: Docker + Docker Compose
- **Web 服务器**: Nginx
- **进程管理**: Gunicorn + Uvicorn workers

## 下一步开发任务

根据 tasks.md，接下来的任务是：

1. **任务 1.2**: 配置数据库连接和 ORM
   - 配置 SQLAlchemy
   - 创建 database.py
   - 配置 Alembic 迁移

2. **任务 1.3**: 配置 Redis 缓存
   - 创建 cache.py
   - 实现缓存装饰器

3. **任务 1.4**: 配置 Elasticsearch
   - 创建 search.py
   - 创建索引映射

4. **任务 2.x**: 创建数据模型
   - User, Table, Column, Lineage 等模型

## 环境变量说明

### 后端环境变量（backend/.env）

```env
DATABASE_URL=mysql+pymysql://user:pass@host:3306/db?charset=utf8mb4
REDIS_URL=redis://localhost:6379/0
ELASTICSEARCH_URL=http://localhost:9200
SECRET_KEY=your-secret-key
DEBUG=false
```

### 前端环境变量（frontend/.env）

```env
VITE_API_BASE_URL=http://localhost:8000
```

### Docker Compose 环境变量（.env）

```env
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=metadata_db
MYSQL_USER=kiro_user
MYSQL_PASSWORD=kiro_password
SECRET_KEY=your-secret-key
```

## 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80, 443 | 反向代理 |
| Frontend | 5173 | Vue 开发服务器 |
| Backend | 8000 | FastAPI 服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Elasticsearch | 9200, 9300 | 搜索引擎 |

## 数据卷

| 卷名 | 用途 |
|------|------|
| mysql_data | MySQL 数据持久化 |
| redis_data | Redis 数据持久化 |
| elasticsearch_data | Elasticsearch 数据持久化 |
| backend_uploads | 上传文件存储 |
| backend_exports | 导出文件存储 |
| backend_logs | 后端日志 |
| nginx_logs | Nginx 日志 |
