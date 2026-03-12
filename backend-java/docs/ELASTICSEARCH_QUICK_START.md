# Elasticsearch 快速开始指南

## 快速启动

### 1. 启动 Elasticsearch

使用 Docker 快速启动 Elasticsearch：

```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

验证启动成功：

```bash
curl http://localhost:9200
```

预期输出：

```json
{
  "name" : "...",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "...",
  "version" : {
    "number" : "8.11.0",
    ...
  },
  "tagline" : "You Know, for Search"
}
```

### 2. 启动应用

应用启动时会自动创建索引：

```bash
mvn spring-boot:run
```

查看日志确认索引创建成功：

```
INFO  c.k.m.c.ElasticsearchIndexConfig - Successfully created index 'table_metadata'
INFO  c.k.m.c.ElasticsearchIndexConfig - Elasticsearch indices initialization completed
```

### 3. 验证索引

```bash
# 查看索引
curl http://localhost:9200/table_metadata

# 查看索引映射
curl http://localhost:9200/table_metadata/_mapping?pretty
```

## 基本使用

### 1. 索引文档

```bash
curl -X POST "localhost:9200/table_metadata/_doc/1" -H 'Content-Type: application/json' -d'
{
  "id": "1",
  "database_name": "test_db",
  "table_name": "user_info",
  "table_type": "TABLE",
  "description": "用户信息表",
  "storage_format": "PARQUET",
  "owner_name": "admin",
  "columns": [
    {
      "column_name": "user_id",
      "data_type": "BIGINT",
      "description": "用户ID"
    },
    {
      "column_name": "user_name",
      "data_type": "VARCHAR",
      "description": "用户名"
    }
  ],
  "tags": ["用户", "核心表"],
  "created_at": "2024-01-15T10:00:00",
  "updated_at": "2024-01-15T10:00:00"
}
'
```

### 2. 搜索文档

```bash
# 搜索表名包含 "user" 的表
curl -X GET "localhost:9200/table_metadata/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "match": {
      "table_name": "user"
    }
  }
}
'

# 搜索描述包含 "用户" 的表
curl -X GET "localhost:9200/table_metadata/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "match": {
      "description": "用户"
    }
  }
}
'

# 多字段搜索
curl -X GET "localhost:9200/table_metadata/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "multi_match": {
      "query": "用户",
      "fields": ["table_name", "description", "columns.column_name"]
    }
  }
}
'
```

### 3. 过滤查询

```bash
# 按数据库名过滤
curl -X GET "localhost:9200/table_metadata/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "term": {
      "database_name": "test_db"
    }
  }
}
'

# 组合查询（搜索 + 过滤）
curl -X GET "localhost:9200/table_metadata/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "description": "用户"
          }
        }
      ],
      "filter": [
        {
          "term": {
            "database_name": "test_db"
          }
        },
        {
          "term": {
            "table_type": "TABLE"
          }
        }
      ]
    }
  }
}
'
```

### 4. 排序和分页

```bash
# 按更新时间倒序排列
curl -X GET "localhost:9200/table_metadata/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "updated_at": {
        "order": "desc"
      }
    }
  ],
  "from": 0,
  "size": 10
}
'
```

### 5. 高亮搜索结果

```bash
curl -X GET "localhost:9200/table_metadata/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "match": {
      "description": "用户"
    }
  },
  "highlight": {
    "fields": {
      "description": {}
    }
  }
}
'
```

## 在 Java 代码中使用

### 1. 创建 Repository

```java
package com.kiro.metadata.repository.elasticsearch;

import com.kiro.metadata.document.TableMetadataDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableMetadataElasticsearchRepository 
        extends ElasticsearchRepository<TableMetadataDocument, String> {
    
    // 按表名搜索
    List<TableMetadataDocument> findByTableNameContaining(String tableName);
    
    // 按数据库名和表名搜索
    List<TableMetadataDocument> findByDatabaseNameAndTableName(
        String databaseName, 
        String tableName
    );
    
    // 按描述搜索
    List<TableMetadataDocument> findByDescriptionContaining(String keyword);
}
```

### 2. 创建 Service

```java
package com.kiro.metadata.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.kiro.metadata.document.TableMetadataDocument;
import com.kiro.metadata.repository.elasticsearch.TableMetadataElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSearchService {
    
    private final ElasticsearchClient elasticsearchClient;
    private final TableMetadataElasticsearchRepository repository;
    
    /**
     * 使用 Repository 搜索
     */
    public List<TableMetadataDocument> searchByTableName(String tableName) {
        return repository.findByTableNameContaining(tableName);
    }
    
    /**
     * 使用 ElasticsearchClient 进行多字段搜索
     */
    public SearchResponse<TableMetadataDocument> multiFieldSearch(String keyword) 
            throws IOException {
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
    
    /**
     * 组合查询（搜索 + 过滤）
     */
    public SearchResponse<TableMetadataDocument> searchWithFilters(
            String keyword, 
            String databaseName, 
            String tableType) throws IOException {
        
        return elasticsearchClient.search(s -> s
            .index("table_metadata")
            .query(q -> q
                .bool(b -> {
                    // 搜索条件
                    if (keyword != null && !keyword.isEmpty()) {
                        b.must(Query.of(mq -> mq
                            .multiMatch(m -> m
                                .query(keyword)
                                .fields("table_name", "description")
                            )
                        ));
                    }
                    
                    // 过滤条件
                    if (databaseName != null && !databaseName.isEmpty()) {
                        b.filter(Query.of(fq -> fq
                            .term(t -> t
                                .field("database_name")
                                .value(databaseName)
                            )
                        ));
                    }
                    
                    if (tableType != null && !tableType.isEmpty()) {
                        b.filter(Query.of(fq -> fq
                            .term(t -> t
                                .field("table_type")
                                .value(tableType)
                            )
                        ));
                    }
                    
                    return b;
                })
            ),
            TableMetadataDocument.class
        );
    }
    
    /**
     * 索引文档
     */
    public void indexDocument(TableMetadataDocument document) {
        repository.save(document);
        log.info("Indexed document: {}", document.getId());
    }
    
    /**
     * 批量索引文档
     */
    public void bulkIndexDocuments(List<TableMetadataDocument> documents) {
        repository.saveAll(documents);
        log.info("Bulk indexed {} documents", documents.size());
    }
    
    /**
     * 删除文档
     */
    public void deleteDocument(String id) {
        repository.deleteById(id);
        log.info("Deleted document: {}", id);
    }
}
```

### 3. 测试

```java
package com.kiro.metadata.service;

import com.kiro.metadata.document.TableMetadataDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ElasticsearchSearchServiceTest {
    
    @Autowired
    private ElasticsearchSearchService searchService;
    
    @Test
    void testIndexAndSearch() {
        // 创建测试文档
        TableMetadataDocument doc = TableMetadataDocument.builder()
            .id("test-1")
            .databaseName("test_db")
            .tableName("user_info")
            .tableType("TABLE")
            .description("用户信息表")
            .storageFormat("PARQUET")
            .ownerName("admin")
            .columns(Arrays.asList(
                TableMetadataDocument.ColumnInfo.builder()
                    .columnName("user_id")
                    .dataType("BIGINT")
                    .description("用户ID")
                    .build()
            ))
            .tags(Arrays.asList("用户", "核心表"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // 索引文档
        searchService.indexDocument(doc);
        
        // 等待索引刷新
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 搜索文档
        List<TableMetadataDocument> results = searchService.searchByTableName("user");
        
        // 验证结果
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTableName()).isEqualTo("user_info");
    }
}
```

## 常用命令

```bash
# 查看所有索引
curl http://localhost:9200/_cat/indices?v

# 查看索引映射
curl http://localhost:9200/table_metadata/_mapping?pretty

# 查看索引设置
curl http://localhost:9200/table_metadata/_settings?pretty

# 查看索引统计
curl http://localhost:9200/table_metadata/_stats?pretty

# 删除索引
curl -X DELETE http://localhost:9200/table_metadata

# 刷新索引
curl -X POST http://localhost:9200/table_metadata/_refresh

# 查看集群健康状态
curl http://localhost:9200/_cluster/health?pretty

# 查看节点信息
curl http://localhost:9200/_nodes?pretty
```

## 故障排查

### 问题 1: 连接失败

```bash
# 检查 Elasticsearch 是否启动
docker ps | grep elasticsearch

# 查看 Elasticsearch 日志
docker logs elasticsearch

# 测试连接
curl http://localhost:9200
```

### 问题 2: 索引未创建

```bash
# 查看应用日志
tail -f logs/application.log | grep Elasticsearch

# 手动创建索引
curl -X PUT "localhost:9200/table_metadata" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "table_name": {
        "type": "text"
      }
    }
  }
}
'
```

### 问题 3: 搜索无结果

```bash
# 检查文档是否已索引
curl http://localhost:9200/table_metadata/_count

# 查看所有文档
curl http://localhost:9200/table_metadata/_search?pretty

# 刷新索引
curl -X POST http://localhost:9200/table_metadata/_refresh
```

## 下一步

1. 实现搜索服务 (Task 7.1-7.4)
2. 实现元数据同步到 Elasticsearch
3. 实现搜索 API 端点
4. 实现前端搜索界面

## 参考资料

- [完整配置文档](../ELASTICSEARCH_CONFIGURATION.md)
- [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Spring Data Elasticsearch](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
