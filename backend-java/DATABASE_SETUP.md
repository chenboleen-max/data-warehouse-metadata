# Database Configuration Guide

## Overview

This document describes the database configuration for the Kiro Metadata Management System.

## Database Requirements

- **Database**: MySQL 8.0+
- **Character Set**: utf8mb4
- **Collation**: utf8mb4_unicode_ci
- **Storage Engine**: InnoDB

## Configuration Files

### 1. application-dev.yml (Development)

Development environment configuration with local MySQL instance:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kiro_metadata?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&connectionCollation=utf8mb4_unicode_ci
    username: root
    password: root
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-test-query: SELECT 1
```

### 2. application-prod.yml (Production)

Production environment configuration with environment variables:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8mb4&useSSL=true&serverTimezone=Asia/Shanghai&connectionCollation=utf8mb4_unicode_ci
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
```

## HikariCP Connection Pool

The application uses HikariCP as the connection pool with the following configuration:

- **minimum-idle**: Minimum number of idle connections (5 for dev, 10 for prod)
- **maximum-pool-size**: Maximum pool size (20 for dev, 50 for prod)
- **idle-timeout**: Maximum idle time (300 seconds)
- **connection-timeout**: Maximum wait time for connection (20 seconds)
- **max-lifetime**: Maximum connection lifetime (1200 seconds / 20 minutes)
- **connection-test-query**: Query to test connection validity (`SELECT 1`)

## JPA/Hibernate Configuration

### Development Environment

```yaml
jpa:
  hibernate:
    ddl-auto: validate  # Validate schema against entities
  show-sql: true  # Show SQL statements in console
  properties:
    hibernate:
      format_sql: true  # Format SQL for readability
      jdbc:
        batch_size: 20  # Batch insert/update size
```

### Production Environment

```yaml
jpa:
  hibernate:
    ddl-auto: validate  # Never auto-create/update schema in production
  show-sql: false  # Disable SQL logging
  properties:
    hibernate:
      format_sql: false
      jdbc:
        batch_size: 50  # Larger batch size for better performance
```

## MyBatis-Plus Configuration

MyBatis-Plus is configured with the following global settings:

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # Convert snake_case to camelCase
    cache-enabled: true  # Enable second-level cache
  global-config:
    db-config:
      id-type: assign_uuid  # Use UUID for primary keys
      logic-delete-field: isDeleted  # Soft delete field
      logic-delete-value: 1  # Value for deleted records
      logic-not-delete-value: 0  # Value for active records
```

## Database Initialization

### Automatic Initialization

The `schema.sql` file in `src/main/resources/` will be executed automatically on first startup to create:

1. All database tables with proper indexes and foreign keys
2. Default users (admin, developer, guest)

### Manual Initialization

To manually initialize the database:

```bash
mysql -u root -p < src/main/resources/schema.sql
```

## Base Entity Configuration

All entities extend `BaseEntity` which provides:

- **UUID Primary Key**: Stored as BINARY(16) for optimal performance
- **Audit Fields**: `createdAt` and `updatedAt` automatically managed
- **Soft Delete**: `isDeleted` flag for logical deletion

Example:

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    // Entity fields...
}
```

## UUID Generation Strategy

UUIDs are generated using Hibernate's UUID generator:

```java
@Id
@GeneratedValue(generator = "UUID")
@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
@Column(columnDefinition = "BINARY(16)")
private UUID id;
```

Benefits:
- Globally unique identifiers
- No database round-trip for ID generation
- Efficient storage as BINARY(16) (16 bytes vs 36 bytes for VARCHAR)

## Character Set Configuration

### Why utf8mb4?

- **Full Unicode Support**: Supports all Unicode characters including emojis
- **4-byte Characters**: Can store characters that require 4 bytes (e.g., 😀)
- **Backward Compatible**: Compatible with utf8 (which only supports 3-byte characters)

### Collation: utf8mb4_unicode_ci

- **Case-Insensitive**: Searches are case-insensitive
- **Accent-Insensitive**: Treats accented characters as equivalent
- **Unicode-Aware**: Proper sorting for international characters

## Database Schema

The database consists of the following tables:

1. **users**: User accounts and authentication
2. **tables**: Table metadata
3. **columns**: Column metadata
4. **lineage**: Data lineage relationships
5. **catalog**: Data catalog/directory structure
6. **table_catalog**: Many-to-many relationship between tables and catalogs
7. **quality_metrics**: Data quality metrics
8. **change_history**: Audit log for metadata changes
9. **export_task**: Export task tracking

## Default Users

The schema creates three default users:

| Username  | Password  | Role      | Email           |
|-----------|-----------|-----------|-----------------|
| admin     | admin123  | ADMIN     | admin@kiro.com  |
| developer | dev123    | DEVELOPER | dev@kiro.com    |
| guest     | guest123  | GUEST     | guest@kiro.com  |

**⚠️ Important**: Change these passwords in production!

## Environment Variables (Production)

Set the following environment variables for production deployment:

```bash
# Database
export DB_HOST=mysql-server
export DB_PORT=3306
export DB_NAME=kiro_metadata
export DB_USERNAME=kiro_user
export DB_PASSWORD=secure_password

# Redis
export REDIS_HOST=redis-server
export REDIS_PORT=6379
export REDIS_PASSWORD=redis_password

# Elasticsearch
export ELASTICSEARCH_URIS=http://elasticsearch:9200
export ELASTICSEARCH_USERNAME=elastic
export ELASTICSEARCH_PASSWORD=elastic_password

# JWT
export JWT_SECRET=your-very-long-random-secret-key-here

# CORS
export CORS_ALLOWED_ORIGINS=https://your-domain.com
```

## Troubleshooting

### Connection Issues

1. **Check MySQL is running**:
   ```bash
   mysql -u root -p -e "SELECT 1"
   ```

2. **Verify database exists**:
   ```bash
   mysql -u root -p -e "SHOW DATABASES LIKE 'kiro_metadata'"
   ```

3. **Test connection from application**:
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

### Character Set Issues

1. **Verify database character set**:
   ```sql
   SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
   FROM information_schema.SCHEMATA 
   WHERE SCHEMA_NAME = 'kiro_metadata';
   ```

2. **Verify table character set**:
   ```sql
   SELECT TABLE_NAME, TABLE_COLLATION 
   FROM information_schema.TABLES 
   WHERE TABLE_SCHEMA = 'kiro_metadata';
   ```

### Performance Issues

1. **Check connection pool stats** via Actuator:
   ```bash
   curl http://localhost:8080/api/actuator/metrics/hikaricp.connections
   ```

2. **Monitor slow queries**:
   ```sql
   SET GLOBAL slow_query_log = 'ON';
   SET GLOBAL long_query_time = 1;
   ```

## Best Practices

1. **Always use connection pooling** (HikariCP is configured by default)
2. **Use prepared statements** (MyBatis-Plus handles this automatically)
3. **Enable batch operations** for bulk inserts/updates
4. **Use indexes** on frequently queried columns (defined in schema.sql)
5. **Monitor connection pool** usage in production
6. **Regular backups** of the database
7. **Use environment variables** for sensitive configuration in production

## References

- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [MySQL Character Sets](https://dev.mysql.com/doc/refman/8.0/en/charset-unicode.html)
- [MyBatis-Plus Documentation](https://baomidou.com/pages/56bac0/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
