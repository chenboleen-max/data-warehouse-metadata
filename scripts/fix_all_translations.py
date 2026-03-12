#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
全面修复被错误翻译的 Java 代码
恢复所有被翻译的变量名、字段名、方法名等
"""

import os
import re
from pathlib import Path

# 完整的恢复映射（中文 -> 英文）
RESTORE_MAPPINGS = {
    # 变量名和字段名（按长度排序，先替换长的）
    "密码Hash": "passwordHash",
    "密码_hash": "password_hash",
    "用户Repository": "userRepository",
    "用户details": "userdetails",
    "用户name": "username",
    "用户Id": "userId",
    "用户s": "users",
    "用户": "user",
    "密码Encoder": "passwordEncoder",
    "密码": "password",
    "数据库Name": "databaseName",
    "数据库": "database",
    "表Type": "tableType",
    "表Id": "tableId",
    "表s": "tables",
    "表": "table",
    "字段Names": "columnNames",
    "字段List": "columnList",
    "字段Name": "columnName",
    "字段": "column",
    "搜索Request": "searchRequest",
    "搜索Service": "searchService",
    "搜索": "search",
    "请求": "request",
    "响应Code": "responseCode",
    "响应": "response",
    "质量Service": "qualityService",
    "质量": "quality",
    "角色": "role",
    "认证": "authentication",
    "授权": "authorization",
    "令牌": "token",
    
    # 包名中的中文
    ".密码": ".password",
    ".用户details": ".userdetails",
    ".查询_dsl": ".query_dsl",
    ".查询": ".query",
    ".响应s": ".responses",
    "io.jsonweb令牌": "io.jsonwebtoken",
    "程序包io.jsonweb令牌": "程序包io.jsonwebtoken",
    "程序包org.springframework.security.crypto.密码": "程序包org.springframework.security.crypto.password",
    "程序包org.springframework.security.core.用户details": "程序包org.springframework.security.core.userdetails",
    "程序包com.baomidou.mybatisplus.core.conditions.查询": "程序包com.baomidou.mybatisplus.core.conditions.query",
    "程序包co.elastic.clients.elasticsearch._types.查询_dsl": "程序包co.elastic.clients.elasticsearch._types.query_dsl",
    "程序包io.swagger.v3.oas.annotations.响应s": "程序包io.swagger.v3.oas.annotations.responses",
    "程序包net.sf.jsqlparser.statement.create.表": "程序包net.sf.jsqlparser.statement.create.table",
    "程序包io.swagger.v3.oas.models.组件s": "程序包io.swagger.v3.oas.models.Components",
    
    # 类名
    "组件s": "Components",
    "Config": "Configuration",
    "ConfigProperties": "ConfigurationProperties",
    "EnableConfigProperties": "EnableConfigurationProperties",
    "RedisCacheConfig": "RedisCacheConfiguration",
    "PasswordEncoder": "PasswordEncoder",
    "CorsConfig": "CorsConfiguration",
    "CorsConfigSource": "CorsConfigurationSource",
    "UrlBasedCorsConfigSource": "UrlBasedCorsConfigurationSource",
    "UserDetailsService": "UserDetailsService",
    "UserDetails": "UserDetails",
    "CreateTable": "CreateTable",
    
    # 常见词汇（在字符串和注释中）
    "成功ly": "successfully",
    "完成": "completed",
    "不存在": "not found",
    "被拒绝": "denied",
    "验证需求": "Validates requirements",
    
    # Java 关键字
    "导入 ": "import ",
}

def restore_file(file_path):
    """恢复单个文件"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 按长度排序，先替换长的字符串（避免部分替换）
        sorted_mappings = sorted(RESTORE_MAPPINGS.items(), key=lambda x: len(x[0]), reverse=True)
        
        # 应用所有恢复映射
        for chinese, english in sorted_mappings:
            content = content.replace(chinese, english)
        
        # 如果内容有变化，写回文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ 已恢复: {file_path}")
            return True
        else:
            return False
            
    except Exception as e:
        print(f"✗ 错误: {file_path} - {e}")
        return False

def main():
    """主函数"""
    # 获取 backend-java 目录
    backend_dir = Path(__file__).parent.parent / 'backend-java' / 'src' / 'main' / 'java'
    
    if not backend_dir.exists():
        print(f"错误: 目录不存在 - {backend_dir}")
        return
    
    print(f"开始全面恢复 Java 文件...")
    print(f"目录: {backend_dir}")
    print("-" * 80)
    
    # 遍历所有 Java 文件
    java_files = list(backend_dir.rglob('*.java'))
    restored_count = 0
    
    for java_file in java_files:
        if restore_file(java_file):
            restored_count += 1
    
    print("-" * 80)
    print(f"完成! 共恢复 {restored_count}/{len(java_files)} 个文件")
    
    if restored_count > 0:
        print(f"\n下一步:")
        print(f"  1. 运行编译检查: cd backend-java && mvn clean compile -DskipTests")
        print(f"  2. 如果仍有错误，查看编译输出并手动修复")
        print(f"  3. 编译通过后，运行测试: mvn test")

if __name__ == '__main__':
    main()
