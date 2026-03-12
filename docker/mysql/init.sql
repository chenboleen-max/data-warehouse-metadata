-- MySQL initialization script
-- This script runs when the MySQL container is first created

-- Set character set and collation
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create database if not exists (already created by MYSQL_DATABASE env var)
-- CREATE DATABASE IF NOT EXISTS metadata_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE metadata_db;

-- Grant privileges to user (already done by Docker, but included for reference)
-- GRANT ALL PRIVILEGES ON metadata_db.* TO 'kiro_user'@'%';
-- FLUSH PRIVILEGES;

-- Create initial tables will be done by Alembic migrations
-- This file is kept for any custom initialization if needed

SELECT 'MySQL initialization completed' AS message;
