# 快速开始指南

## 一键启动（推荐）

使用 Docker Compose 快速启动整个系统：

```bash
# 1. 进入项目目录
cd kiro_web

# 2. 复制环境变量文件
cp .env.example .env

# 3. 编辑 .env 文件（可选，使用默认值也可以）
# 重要：生产环境请务必修改 SECRET_KEY 和数据库密码

# 4. 启动所有服务
docker-compose up -d

# 5. 查看服务状态
docker-compose ps

# 6. 查看日志
docker-compose logs -f
```

## 访问应用

启动成功后，访问以下地址：

- **前端应用**: http://localhost:80
- **后端 API**: http://localhost:80/api
- **API 文档**: http://localhost:80/api/docs
- **MySQL**: localhost:3306
- **Redis**: localhost:6379
- **Elasticsearch**: http://localhost:9200

## 默认账号

系统首次启动后，需要通过 API 创建管理员账号（后续任务会实现）。

## 常用命令

### 使用 Makefile（推荐）

```bash
# 查看所有可用命令
make help

# 启动服务
make up

# 停止服务
make down

# 重启服务
make restart

# 查看日志
make logs

# 清理所有数据（包括数据库）
make clean

# 重新构建镜像
make build

# 进入后端容器
make backend-shell

# 进入前端容器
make frontend-shell

# 进入 MySQL
make db-shell
```

### 使用 Docker Compose

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 查看日志
docker-compose logs -f [service_name]

# 重启特定服务
docker-compose restart [service_name]

# 查看服务状态
docker-compose ps

# 进入容器
docker-compose exec [service_name] /bin/bash
```

## 本地开发模式

如果你想在本地开发而不使用 Docker：

### 后端开发

```bash
# 1. 确保 MySQL、Redis、Elasticsearch 正在运行
# 可以只启动这些服务：
docker-compose up -d mysql redis elasticsearch

# 2. 进入后端目录
cd backend

# 3. 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 或 venv\Scripts\activate  # Windows

# 4. 安装依赖
pip install -r requirements.txt

# 5. 配置环境变量
cp .env.example .env
# 编辑 .env，设置数据库连接等

# 6. 启动开发服务器
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 前端开发

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 配置环境变量
cp .env.example .env

# 4. 启动开发服务器
npm run dev
```

## 验证安装

### 1. 检查后端健康状态

```bash
curl http://localhost:8000/api/health
```

应该返回：
```json
{
  "status": "healthy",
  "version": "1.0.0"
}
```

### 2. 检查前端

在浏览器中访问 http://localhost:5173（开发模式）或 http://localhost:80（Docker 模式）

### 3. 检查数据库

```bash
# 进入 MySQL 容器
docker-compose exec mysql mysql -u root -p

# 输入密码（默认：rootpassword）
# 然后执行：
SHOW DATABASES;
USE metadata_db;
SHOW TABLES;
```

### 4. 检查 Redis

```bash
docker-compose exec redis redis-cli ping
# 应该返回：PONG
```

### 5. 检查 Elasticsearch

```bash
curl http://localhost:9200
# 应该返回 Elasticsearch 版本信息
```

## 故障排查

### 端口冲突

如果端口被占用，修改 `.env` 文件中的端口配置：

```env
MYSQL_PORT=3307
REDIS_PORT=6380
ELASTICSEARCH_PORT=9201
BACKEND_PORT=8001
FRONTEND_PORT=5174
NGINX_HTTP_PORT=8080
```

### 容器启动失败

查看具体服务的日志：

```bash
docker-compose logs [service_name]
```

### 数据库连接失败

1. 确保 MySQL 容器已启动并健康：
```bash
docker-compose ps mysql
```

2. 检查数据库连接字符串是否正确

3. 等待 MySQL 完全启动（首次启动需要初始化）

### 清理并重新开始

```bash
# 停止所有服务
docker-compose down

# 删除所有数据卷（警告：会删除所有数据）
docker-compose down -v

# 重新启动
docker-compose up -d
```

## 下一步

1. 查看 [README.md](README.md) 了解项目详情
2. 查看 [backend/README.md](backend/README.md) 了解后端开发
3. 查看 [frontend/README.md](frontend/README.md) 了解前端开发
4. 查看 API 文档：http://localhost:8000/api/docs

## 需要帮助？

- 查看项目文档
- 提交 Issue
- 联系开发团队
