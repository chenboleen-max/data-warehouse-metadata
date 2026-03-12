# Elasticsearch 配置文档

## 概述

本文档介绍数据仓库元数据管理系统中 Elasticsearch 搜索引擎的配置和使用方法。

## 技术栈

- **Elasticsearch**: 8.x
- **Spring Data Elasticsearch**: 5.2.x (Spring Boot 3.2.1)
- **Elasticsearch Java API Client**: 8.x (新版客户端，替代已废弃的 RestHighLevelClient)

## 配置说明

### 1. 依赖配置

在 `pom.xml` 中已包含以下依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### 2. 应用配置

#### 开发环境 (application-dev.yml)

```yaml
spring:
  data:
    elasticsearch:
      repositories:
        enabled: true
  
  elasticsearch:
    uris: http://localhost:9200
    username: 
    password: 
    connection-timeout: 5s
    socket-timeout: 30s
```

#### 生产环境 (application-prod.yml)

```yaml
spring:
  data:
    elasticsearch:
      repositories:
        enabled: true
  
  elasticsearch:
    uris: ${ELASTICSEARCH_URIS:http://localhost:9200}
    username: ${ELASTICSEARCH_USERNAME:}
    password: ${ELASTICSEARCH_PASSWORD:}
    connection-timeout: 5s
    socket-timeout: 30s
```

**配置说明：**
- `uris`: Elasticsearch 服务地址，支持多个节点（逗号分隔）
- `username`: 认证用户名（可选）
- `password`: 认证密码（可选）
- `connection-timeout`: 连接超时时间
- `socket-timeout`: Socket 超时时间

### 3. 配置类

#### ElasticsearchConfig.java

配置 Elasticsearch 客户端连接：

```java
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.kiro.metadata.repository.elasticsearch")
public class ElasticsearchConfig {
    
    @Bean
    public RestClient restClient() {
        // 配置 RestClient
    }
    
    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        // 配置传输层
    }
    
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        // 配置 Elasticsearch 客户端
    }
}
```

**功能：**
- 配置 Elasticsearch 客户端连接
- 支持用户名密码认证
- 配置超时时间
- 配置 JSON 序列化（支持 Java 8 时间类型）

#### ElasticsearchIndexConfig.java

配置索引映射和自动创建索引：

```java
@Component
public class ElasticsearchIndexConfig {
    
    @EventListener(ApplicationReadyEvent.class)
    public void createIndicesOnStartup() {
        // 应用启动时自动创建索引
    }
    
    public void createTableMetadataIndex() {
        // 创建表元数据索引
    }
}
```

**功能：**
- 定义表元数据索引结构
- 应用启动时自动创建索引（如果不存在）
- 提供索引管理方法（创建、删除、重建）

## 索引结构

### table_metadata 索引

表元数据索引用于存储和搜索数据表的元数据信息。

#### 字段映射

| 字段名 | 类型 | 说明 | 分析器 |
|--------|------|------|--------|
| id | keyword | 表 ID | - |
| database_name | keyword | 数据库名 | - |
| table_name | text + keyword | 表名 | standard |
| table_type | keyword | 表类型 | - |
| description | text | 表描述 | standard |
| storage_format | keyword | 存储格式 | - |
| storage_location | keyword | 存储位置 | - |
| owner_name | keyword | 所有者名称 | - |
| columns | nested | 字段列表 | - |
| columns.column_name | text | 字段名 | standard |
| columns.data_type | keyword | 数据类型 | - |
| columns.description | text | 字段描述 | standard |
| tags | keyword | 标签列表 | - |
| created_at | date | 创建时间 | - |
| updated_at | date | 更新时间 | - |

#### 字段类型说明

- **keyword**: 精确匹配，不分词，用于过滤、排序、聚合
- **text**: 全文搜索，分词，用于搜索
- **nested**: 嵌套对象，保持对象结构
- **date**: 日期类型，支持多种格式

#### 分析器说明

- **standard**: Elasticsearch 内置标准分析器
  - 支持英文分词
  - 支持基本的中文分词（按字符分词）
  - 生产环境建议安装 IK 分词器插件以获得更好的中文分词效果

## 使用示例

### 1. 文档类

```java
@Document(indexName = "table_metadata")
public class TableMetadataDocument {
    @Id
    private String id;
    
    @Field(type = FieldType.Keyword)
    private String databaseName;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String tableName;
    
    // ... 其他字段
}
```

### 2. Repository 接口

```java
public interface TableMetadataRepository extends ElasticsearchRepository<TableMetadataDocument, String> {
    
    // 按表名搜索
    List<TableMetadataDocument> findByTableNameContaining(String tableName);
    
    // 按数据库名和表名搜索
    List<TableMetadataDocument> findByDatabaseNameAndTableName(String databaseName, String tableName);
}
```

### 3. 使用 ElasticsearchClient

```java
@Service
public class SearchService {
    
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    public SearchResponse<TableMetadataDocument> search(String keyword) throws IOException {
        return elasticsearchClient.search(s -> s
            .index("table_metadata")
            .query(q -> q
                .multiMatch(m -> m
                    .query(keyword)
                    .fields("table_name", "description", "columns.column_name")
                )
            ),
            TableMetadataDocument.class
        );
    }
}
```

## 安装和启动 Elasticsearch

### 使用 Docker

```bash
# 拉取 Elasticsearch 8.x 镜像
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# 启动 Elasticsearch（单节点模式）
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# 验证 Elasticsearch 是否启动成功
curl http://localhost:9200
```

### 使用 Docker Compose

在项目根目录的 `docker-compose.yml` 中添加：

```yaml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: kiro-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - kiro-network

volumes:
  elasticsearch-data:
    driver: local
```

启动：

```bash
docker-compose up -d elasticsearch
```

## 索引管理

### 创建索引

索引会在应用启动时自动创建。如果需要手动创建：

```java
@Autowired
private ElasticsearchIndexConfig indexConfig;

// 创建索引
indexConfig.createTableMetadataIndex();
```

### 删除索引

```java
// 删除索引
indexConfig.deleteTableMetadataIndex();
```

### 重建索引

```java
// 重建索引（先删除后创建）
indexConfig.rebuildTableMetadataIndex();
```

### 使用 REST API

```bash
# 查看索引
curl -X GET "localhost:9200/table_metadata"

# 查看索引映射
curl -X GET "localhost:9200/table_metadata/_mapping"

# 删除索引
curl -X DELETE "localhost:9200/table_metadata"

# 搜索文档
curl -X GET "localhost:9200/table_metadata/_search?q=table_name:user"
```

## 中文分词优化

### 安装 IK 分词器

生产环境建议安装 IK 分词器以获得更好的中文分词效果。

#### 1. 下载 IK 分词器

```bash
# 下载与 Elasticsearch 版本匹配的 IK 分词器
# https://github.com/medcl/elasticsearch-analysis-ik/releases
```

#### 2. 安装插件

```bash
# 进入 Elasticsearch 容器
docker exec -it elasticsearch bash

# 安装 IK 分词器
./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.11.0/elasticsearch-analysis-ik-8.11.0.zip

# 重启 Elasticsearch
docker restart elasticsearch
```

#### 3. 修改分析器配置

在 `ElasticsearchIndexConfig.java` 中修改分析器：

```java
// 将 analyzer("standard") 改为 analyzer("ik_max_word")
properties.put("description", Property.of(p -> p
    .text(TextProperty.of(t -> t
        .analyzer("ik_max_word")  // 使用 IK 分词器
    ))
));
```

#### 4. 测试分词效果

```bash
# 测试 IK 分词器
curl -X POST "localhost:9200/_analyze" -H 'Content-Type: application/json' -d'
{
  "analyzer": "ik_max_word",
  "text": "数据仓库元数据管理系统"
}
'
```

## 性能优化

### 1. 批量索引

使用批量操作提高索引性能：

```java
BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

for (TableMetadataDocument doc : documents) {
    bulkBuilder.operations(op -> op
        .index(idx -> idx
            .index("table_metadata")
            .id(doc.getId())
            .document(doc)
        )
    );
}

BulkResponse response = elasticsearchClient.bulk(bulkBuilder.build());
```

### 2. 分片和副本配置

```java
.settings(IndexSettings.of(s -> s
    .numberOfShards("3")      // 根据数据量调整分片数
    .numberOfReplicas("1")    // 根据可用性要求调整副本数
))
```

### 3. 刷新间隔

```java
.settings(IndexSettings.of(s -> s
    .refreshInterval(Time.of(t -> t.time("30s")))  // 调整刷新间隔
))
```

## 故障排查

### 1. 连接失败

**问题**: 应用无法连接到 Elasticsearch

**解决方案**:
- 检查 Elasticsearch 是否启动：`curl http://localhost:9200`
- 检查配置文件中的 `uris` 是否正确
- 检查防火墙设置
- 查看应用日志：`tail -f logs/application.log`

### 2. 索引创建失败

**问题**: 索引创建失败

**解决方案**:
- 检查 Elasticsearch 日志：`docker logs elasticsearch`
- 检查索引映射配置是否正确
- 检查 Elasticsearch 磁盘空间是否充足

### 3. 搜索性能慢

**问题**: 搜索响应时间过长

**解决方案**:
- 使用 `_explain` API 分析查询性能
- 优化查询条件，避免使用通配符开头的查询
- 增加分片数
- 使用缓存
- 考虑使用 `scroll` API 进行深度分页

### 4. 中文分词效果不佳

**问题**: 中文搜索结果不准确

**解决方案**:
- 安装 IK 分词器插件
- 使用 `ik_max_word` 或 `ik_smart` 分析器
- 测试分词效果：`POST /_analyze`

## 监控和维护

### 1. 查看集群健康状态

```bash
curl -X GET "localhost:9200/_cluster/health?pretty"
```

### 2. 查看索引统计信息

```bash
curl -X GET "localhost:9200/table_metadata/_stats?pretty"
```

### 3. 查看节点信息

```bash
curl -X GET "localhost:9200/_nodes/stats?pretty"
```

### 4. 清理旧数据

```bash
# 删除 30 天前的数据
curl -X POST "localhost:9200/table_metadata/_delete_by_query" -H 'Content-Type: application/json' -d'
{
  "query": {
    "range": {
      "created_at": {
        "lt": "now-30d"
      }
    }
  }
}
'
```

## 参考资料

- [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Spring Data Elasticsearch 文档](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
- [Elasticsearch Java API Client 文档](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html)
- [IK 分词器 GitHub](https://github.com/medcl/elasticsearch-analysis-ik)

## 下一步

完成 Elasticsearch 配置后，可以继续实现：

1. **搜索服务** (Task 7.1-7.4)：实现全文搜索、过滤、排序等功能
2. **元数据同步**：在创建/更新表元数据时同步到 Elasticsearch
3. **搜索 API**：实现搜索相关的 REST API 端点
4. **前端搜索页面**：实现搜索界面和结果展示

## 联系方式

如有问题，请联系开发团队或查看项目文档。
