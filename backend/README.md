# Backend - 数据仓库元数据管理系统

FastAPI 后端服务

## 技术栈

- Python 3.9+
- FastAPI
- SQLAlchemy 2.0
- MySQL 8.0+ (使用 PyMySQL 驱动)
- Redis 7.x
- Elasticsearch 8.x
- JWT 认证

## 项目结构

```
backend/
├── app/
│   ├── api/                # API 路由
│   │   ├── routes/        # 路由模块
│   │   └── dependencies.py # 依赖注入
│   ├── core/              # 核心配置
│   │   └── config.py      # 应用配置
│   ├── models/            # SQLAlchemy 模型
│   ├── schemas/           # Pydantic 模式
│   ├── services/          # 业务逻辑服务
│   └── main.py            # 应用入口
├── tests/                 # 测试
│   ├── unit/             # 单元测试
│   ├── property/         # 基于属性的测试
│   └── integration/      # 集成测试
├── alembic/              # 数据库迁移
├── requirements.txt      # 依赖列表
├── pyproject.toml        # Poetry 配置
└── Dockerfile            # Docker 镜像
```

## 开发环境设置

### 1. 创建虚拟环境

```bash
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 或
venv\Scripts\activate  # Windows
```

### 2. 安装依赖

```bash
pip install -r requirements.txt
```

或使用 Poetry:

```bash
poetry install
```

### 3. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 文件，设置数据库连接等配置。

### 4. 启动开发服务器

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

访问 API 文档: http://localhost:8000/api/docs

## 数据库

### MySQL 配置

系统使用 MySQL 8.0+ 作为主数据库，配置要求：

- 字符集: `utf8mb4`
- 排序规则: `utf8mb4_unicode_ci`
- 驱动: PyMySQL (纯 Python 实现，无需编译)

连接字符串格式：
```
mysql+pymysql://user:password@host:port/database?charset=utf8mb4
```

### 数据库迁移

使用 Alembic 管理数据库迁移：

```bash
# 创建迁移
alembic revision --autogenerate -m "描述"

# 应用迁移
alembic upgrade head

# 回滚迁移
alembic downgrade -1
```

## 测试

### 运行所有测试

```bash
pytest tests/
```

### 运行单元测试

```bash
pytest tests/unit/
```

### 运行属性测试

```bash
pytest tests/property/ -m property
```

### 测试覆盖率

```bash
pytest --cov=app --cov-report=html
```

## API 文档

启动服务后访问：

- Swagger UI: http://localhost:8000/api/docs
- ReDoc: http://localhost:8000/api/redoc
- OpenAPI JSON: http://localhost:8000/api/openapi.json

## 代码质量

### 格式化代码

```bash
black app/
isort app/
```

### 类型检查

```bash
mypy app/
```

### Lint 检查

```bash
flake8 app/
```

## 部署

### 使用 Docker

```bash
docker build -t kiro-web-backend .
docker run -p 8000:8000 --env-file .env kiro-web-backend
```

### 使用 Gunicorn

```bash
gunicorn app.main:app \
  --workers 4 \
  --worker-class uvicorn.workers.UvicornWorker \
  --bind 0.0.0.0:8000
```

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DATABASE_URL | MySQL 连接字符串 | - |
| REDIS_URL | Redis 连接字符串 | - |
| ELASTICSEARCH_URL | Elasticsearch 地址 | - |
| SECRET_KEY | JWT 密钥 | - |
| DEBUG | 调试模式 | false |
| LOG_LEVEL | 日志级别 | INFO |

详见 `.env.example` 文件。
