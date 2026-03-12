-- Database initialization script for Kiro Metadata Management System
-- MySQL 8.0+ with utf8mb4 character set and utf8mb4_unicode_ci collation

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS kiro_metadata
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE kiro_metadata;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    last_login_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tables metadata
CREATE TABLE IF NOT EXISTS tables (
    id BINARY(16) NOT NULL PRIMARY KEY,
    database_name VARCHAR(100) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    table_type VARCHAR(20) NOT NULL,
    description VARCHAR(1000),
    storage_format VARCHAR(50),
    storage_location VARCHAR(500),
    data_size_bytes BIGINT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    last_accessed_at DATETIME(6),
    owner_id BINARY(16) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_database_table (database_name, table_name),
    INDEX idx_updated_at (updated_at),
    INDEX idx_database_name (database_name),
    INDEX idx_table_name (table_name),
    INDEX idx_owner_id (owner_id),
    CONSTRAINT fk_tables_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Columns metadata
CREATE TABLE IF NOT EXISTS columns (
    id BINARY(16) NOT NULL PRIMARY KEY,
    table_id BINARY(16) NOT NULL,
    column_name VARCHAR(100) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    column_order INT NOT NULL,
    is_nullable BOOLEAN NOT NULL DEFAULT TRUE,
    is_partition_key BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_table_order (table_id, column_order),
    INDEX idx_table_name (table_id, column_name),
    CONSTRAINT fk_columns_table FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Lineage relationships
CREATE TABLE IF NOT EXISTS lineage (
    id BINARY(16) NOT NULL PRIMARY KEY,
    source_table_id BINARY(16) NOT NULL,
    target_table_id BINARY(16) NOT NULL,
    lineage_type VARCHAR(20) NOT NULL,
    transformation_logic TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by BINARY(16) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_source_target (source_table_id, target_table_id),
    INDEX idx_source (source_table_id),
    INDEX idx_target (target_table_id),
    CONSTRAINT fk_lineage_source FOREIGN KEY (source_table_id) REFERENCES tables(id) ON DELETE CASCADE,
    CONSTRAINT fk_lineage_target FOREIGN KEY (target_table_id) REFERENCES tables(id) ON DELETE CASCADE,
    CONSTRAINT fk_lineage_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Catalog (data directory)
CREATE TABLE IF NOT EXISTS catalog (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    parent_id BINARY(16),
    level INT NOT NULL,
    path VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by BINARY(16) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_parent (parent_id),
    INDEX idx_path (path),
    INDEX idx_level (level),
    CONSTRAINT fk_catalog_parent FOREIGN KEY (parent_id) REFERENCES catalog(id) ON DELETE CASCADE,
    CONSTRAINT fk_catalog_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_level CHECK (level >= 1 AND level <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table-Catalog relationship (many-to-many)
CREATE TABLE IF NOT EXISTS table_catalog (
    table_id BINARY(16) NOT NULL,
    catalog_id BINARY(16) NOT NULL,
    PRIMARY KEY (table_id, catalog_id),
    CONSTRAINT fk_table_catalog_table FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE CASCADE,
    CONSTRAINT fk_table_catalog_catalog FOREIGN KEY (catalog_id) REFERENCES catalog(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Quality metrics
CREATE TABLE IF NOT EXISTS quality_metrics (
    id BINARY(16) NOT NULL PRIMARY KEY,
    table_id BINARY(16) NOT NULL,
    record_count BIGINT,
    null_rate DECIMAL(5,4),
    update_frequency VARCHAR(20),
    data_freshness_hours INT,
    measured_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_table_measured (table_id, measured_at),
    CONSTRAINT fk_quality_table FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE CASCADE,
    CONSTRAINT chk_null_rate CHECK (null_rate >= 0.0 AND null_rate <= 1.0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Change history
CREATE TABLE IF NOT EXISTS change_history (
    id BINARY(16) NOT NULL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BINARY(16) NOT NULL,
    operation VARCHAR(20) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    changed_at DATETIME(6) NOT NULL,
    changed_by BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_entity_changed (entity_type, entity_id, changed_at),
    INDEX idx_changed_by (changed_by),
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Export tasks
CREATE TABLE IF NOT EXISTS export_task (
    id BINARY(16) NOT NULL PRIMARY KEY,
    task_type VARCHAR(20) NOT NULL,
    filters JSON,
    status VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    record_count INT,
    error_message TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by BINARY(16) NOT NULL,
    started_at DATETIME(6),
    completed_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_created (created_by, created_at),
    INDEX idx_status (status),
    CONSTRAINT fk_export_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user (password: admin123)
-- BCrypt hash for 'admin123'
INSERT INTO users (id, username, email, password_hash, role, is_active, created_at, updated_at, is_deleted)
VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'admin',
    'admin@kiro.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    TRUE,
    NOW(6),
    NOW(6),
    FALSE
) ON DUPLICATE KEY UPDATE username=username;

-- Insert test developer user (password: dev123)
INSERT INTO users (id, username, email, password_hash, role, is_active, created_at, updated_at, is_deleted)
VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'developer',
    'dev@kiro.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'DEVELOPER',
    TRUE,
    NOW(6),
    NOW(6),
    FALSE
) ON DUPLICATE KEY UPDATE username=username;

-- Insert test guest user (password: guest123)
INSERT INTO users (id, username, email, password_hash, role, is_active, created_at, updated_at, is_deleted)
VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'guest',
    'guest@kiro.com',
    '$2a$10$DpwmetHYmqvgGqhXPx8iBu4.KqVXQqgqQqXqQqXqQqXqQqXqQqXqQ',
    'GUEST',
    TRUE,
    NOW(6),
    NOW(6),
    FALSE
) ON DUPLICATE KEY UPDATE username=username;
