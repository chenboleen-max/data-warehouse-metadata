# 部署指南

## 环境要求

### 硬件要求
- CPU: 4核心或以上
- 内存: 8GB 或以上
- 磁盘: 50GB 或以上（SSD 推荐）

### 软件要求
- Docker: 20.10+ 
- Docker Compose: 2.0+
- Git: 2.0+

## 快速开始

### 1. 克隆代码

```bash
git clone <repository-url>
cd kiro_web
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量
vi .env
```

**重要**：请修改以下配置：
- `MYSQL_ROOT_PASSWORD`: MySQL root 密码
- `MYSQL_PASSWORD`: 应用数据库密码
- `JWT_SECRET`: JWT 密钥（至少 256 位）
- `REDIS_PASSWORD`: Redis 密码（可选）

### 3. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 访问应用

- 前端: http://localhost
- 后端 API: http://localhost:8080
- API 文档: http://localhost:8080/api/swagger-ui.html

### 5. 默认账号

首次启动后，系统会创建默认管理员账号：
- 用户名: `admin`
- 密码: `admin123`

**重要**：请立即修改默认密码！

## 详细部署步骤

### 步骤 1: 准备服务器

#### Ubuntu/Debian

```bash
# 更新系统
sudo apt-get update
sudo apt-get upgrade -y

# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### CentOS/RHEL

```bash
# 安装 Docker
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 步骤 2: 配置防火墙

```bash
# Ubuntu/Debian (UFW)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw enable

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### 步骤 3: 配置 HTTPS（可选但推荐）

#### 使用 Let's Encrypt

```bash
# 安装 Certbot
sudo apt-get install certbot

# 获取证书
sudo certbot certonly --standalone -d your-domain.com

# 证书路径
# /etc/letsencrypt/live/your-domain.com/fullchain.pem
# /etc/letsencrypt/live/your-domain.com/privkey.pem
```

#### 更新 Nginx 配置

编辑 `frontend/nginx.conf`：

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # ... 其他配置
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

更新 `docker-compose.yml`：

```yaml
frontend:
  # ...
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - /etc/letsencrypt:/etc/letsencrypt:ro
```

### 步骤 4: 数据库初始化

数据库会在首次启动时自动初始化。如果需要手动初始化：

```bash
# 进入 MySQL 容器
docker-compose exec mysql mysql -u root -p

# 执行初始化脚本
mysql> source /docker-entrypoint-initdb.d/schema.sql;
```

### 步骤 5: 验证部署

```bash
# 检查所有服务是否运行
docker-compose ps

# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查前端
curl http://localhost/

# 查看日志
docker-compose logs backend
docker-compose logs frontend
```

## 服务管理

### 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 启动特定服务
docker-compose up -d backend
```

### 停止服务

```bash
# 停止所有服务
docker-compose stop

# 停止特定服务
docker-compose stop backend
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
```

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend

# 查看最近 100 行日志
docker-compose logs --tail=100 backend
```

### 更新服务

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose up -d --build

# 或者分步执行
docker-compose build
docker-compose up -d
```

### 清理资源

```bash
# 停止并删除容器
docker-compose down

# 删除容器和卷（会删除数据！）
docker-compose down -v

# 清理未使用的镜像
docker image prune -a
```

## 数据备份

### 备份 MySQL 数据

```bash
# 备份数据库
docker-compose exec mysql mysqldump -u root -p metadata_db > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker-compose exec -T mysql mysql -u root -p metadata_db < backup_20240101.sql
```

### 备份 Redis 数据

```bash
# Redis 使用 AOF 持久化，数据保存在卷中
docker-compose exec redis redis-cli BGSAVE

# 复制 RDB 文件
docker cp metadata-redis:/data/dump.rdb ./backup/redis_$(date +%Y%m%d).rdb
```

### 备份 Elasticsearch 数据

```bash
# 创建快照仓库
curl -X PUT "localhost:9200/_snapshot/backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/usr/share/elasticsearch/backup"
  }
}'

# 创建快照
curl -X PUT "localhost:9200/_snapshot/backup/snapshot_$(date +%Y%m%d)"
```

## 监控和维护

### 查看资源使用

```bash
# 查看容器资源使用
docker stats

# 查看磁盘使用
docker system df
```

### 日志轮转

编辑 `/etc/docker/daemon.json`：

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

重启 Docker：

```bash
sudo systemctl restart docker
```

### 定时任务

创建备份脚本 `/opt/metadata-backup.sh`：

```bash
#!/bin/bash
BACKUP_DIR="/backup/metadata"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份 MySQL
docker-compose -f /path/to/docker-compose.yml exec -T mysql \
  mysqldump -u root -p$MYSQL_ROOT_PASSWORD metadata_db \
  > $BACKUP_DIR/mysql_$DATE.sql

# 压缩备份
gzip $BACKUP_DIR/mysql_$DATE.sql

# 删除 7 天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
```

添加到 crontab：

```bash
# 每天凌晨 2 点备份
0 2 * * * /opt/metadata-backup.sh
```

## 故障排查

### 服务无法启动

```bash
# 查看详细日志
docker-compose logs backend

# 检查端口占用
sudo netstat -tulpn | grep :8080

# 检查磁盘空间
df -h

# 检查内存使用
free -h
```

### 数据库连接失败

```bash
# 检查 MySQL 是否运行
docker-compose ps mysql

# 检查 MySQL 日志
docker-compose logs mysql

# 测试连接
docker-compose exec mysql mysql -u root -p -e "SELECT 1"
```

### 前端无法访问后端

```bash
# 检查网络连接
docker-compose exec frontend ping backend

# 检查 Nginx 配置
docker-compose exec frontend nginx -t

# 重新加载 Nginx
docker-compose exec frontend nginx -s reload
```

### 性能问题

```bash
# 查看容器资源使用
docker stats

# 查看 MySQL 慢查询
docker-compose exec mysql mysql -u root -p -e "SHOW VARIABLES LIKE 'slow_query_log%'"

# 查看 Redis 信息
docker-compose exec redis redis-cli INFO
```

## 生产环境建议

### 安全配置

1. 修改所有默认密码
2. 启用 HTTPS
3. 配置防火墙
4. 定期更新系统和依赖
5. 启用审计日志
6. 配置备份策略

### 性能优化

1. 使用 SSD 存储
2. 增加内存和 CPU
3. 配置 Redis 持久化
4. 优化 MySQL 配置
5. 启用 CDN
6. 配置负载均衡

### 高可用配置

1. 使用 MySQL 主从复制
2. 使用 Redis 哨兵或集群
3. 使用 Elasticsearch 集群
4. 配置多个后端实例
5. 使用 Nginx 负载均衡

## 常见问题

### Q: 如何修改端口？

A: 编辑 `docker-compose.yml` 中的 `ports` 配置：

```yaml
frontend:
  ports:
    - "8000:80"  # 将前端端口改为 8000
```

### Q: 如何增加内存限制？

A: 在 `docker-compose.yml` 中添加资源限制：

```yaml
backend:
  deploy:
    resources:
      limits:
        memory: 2G
      reservations:
        memory: 1G
```

### Q: 如何查看应用版本？

A: 访�� `/actuator/info` 端点：

```bash
curl http://localhost:8080/actuator/info
```

## 联系支持

如有问题，请联系：
- 邮箱: support@example.com
- 文档: https://docs.example.com
- Issue: https://github.com/example/metadata/issues
