#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将 Java 代码中的英文注释和提示信息翻译成中文
"""

import os
import re
from pathlib import Path

# 英文到中文的映射
TRANSLATIONS = {
    # 通用注释
    "// Generate": "// 生成",
    "// Record": "// 记录",
    "// Get": "// 获取",
    "// Set": "// 设置",
    "// Check": "// 检查",
    "// Validate": "// 验证",
    "// Calculate": "// 计算",
    "// Clear": "// 清除",
    "// Handle": "// 处理",
    "// Log": "// 记录日志",
    "// Find": "// 查找",
    "// Create": "// 创建",
    "// Update": "// 更新",
    "// Delete": "// 删除",
    "// Add": "// 添加",
    "// Remove": "// 移除",
    
    # 具体注释
    "// Generate request ID": "// 生成请求 ID",
    "// Record start time": "// 记录开始时间",
    "// Get user ID from security context": "// 从安全上下文获取用户 ID",
    "// Log request information": "// 记录请求信息",
    "// Calculate processing time": "// 计算处理时间",
    "// Set response header": "// 设置响应头",
    "// Log response information": "// 记录响应信息",
    "// Clear MDC to prevent memory leaks": "// 清除 MDC 防止内存泄漏",
    "// Handle multiple IPs in X-Forwarded-For (take the first one)": "// 处理 X-Forwarded-For 中的多个 IP（取第一个）",
    
    # 异常消息
    "User not found": "用户不存在",
    "Account is disabled": "账号已被禁用",
    "Invalid username or password": "用户名或密码错误",
    "Invalid refresh token": "刷新令牌无效",
    "No authenticated user": "没有已认证的用户",
    "Permission denied": "权限不足",
    "Resource not found": "资源不存在",
    "Invalid token": "无效的令牌",
    "Token expired": "令牌已过期",
    "Unauthorized": "未授权",
    "Forbidden": "禁止访问",
    "Bad request": "请求错误",
    "Internal server error": "服务器内部错误",
    
    # 日志消息
    "Login failed": "登录失败",
    "Login successful": "登录成功",
    "Logout failed": "登出失败",
    "Logout successful": "登出成功",
    "Token refresh failed": "令牌刷新失败",
    "Token refresh successful": "令牌刷新成功",
    "Permission check failed": "权限检查失败",
    "User login attempt": "用户登录尝试",
    "User logout attempt": "用户登出尝试",
    "Token refresh attempt": "令牌刷新尝试",
    
    # 其他常见短语
    "user not found": "用户不存在",
    "account is disabled": "账号已被禁用",
    "invalid password": "密码错误",
    "invalid token": "令牌无效",
}

def translate_file(file_path):
    """翻译单个文件"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 应用所有翻译
        for english, chinese in TRANSLATIONS.items():
            content = content.replace(english, chinese)
        
        # 如果内容有变化，写回文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ 已翻译: {file_path}")
            return True
        else:
            print(f"- 无需翻译: {file_path}")
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
    
    print(f"开始翻译 Java 文件...")
    print(f"目录: {backend_dir}")
    print("-" * 60)
    
    # 遍历所有 Java 文件
    java_files = list(backend_dir.rglob('*.java'))
    translated_count = 0
    
    for java_file in java_files:
        if translate_file(java_file):
            translated_count += 1
    
    print("-" * 60)
    print(f"完成! 共翻译 {translated_count}/{len(java_files)} 个文件")

if __name__ == '__main__':
    main()
