#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量翻译 Java 代码中的英文注释和提示信息
"""

import os
import re
from pathlib import Path

# 英文到中文的映射（扩展版）
TRANSLATIONS = {
    # 类注释
    "Service": "服务",
    "Controller": "控制器",
    "Repository": "仓库",
    "Configuration": "配置",
    "Component": "组件",
    "Filter": "过滤器",
    "Interceptor": "拦截器",
    "Aspect": "切面",
    "Exception": "异常",
    "Handler": "处理器",
    
    # 方法注释开头
    "/**\n * Create ": "/**\n * 创建",
    "/**\n * Update ": "/**\n * 更新",
    "/**\n * Delete ": "/**\n * 删除",
    "/**\n * Get ": "/**\n * 获取",
    "/**\n * Find ": "/**\n * 查找",
    "/**\n * Search ": "/**\n * 搜索",
    "/**\n * List ": "/**\n * 列出",
    "/**\n * Query ": "/**\n * 查询",
    "/**\n * Save ": "/**\n * 保存",
    "/**\n * Build ": "/**\n * 构建",
    "/**\n * Generate ": "/**\n * 生成",
    "/**\n * Validate ": "/**\n * 验证",
    "/**\n * Check ": "/**\n * 检查",
    "/**\n * Calculate ": "/**\n * 计算",
    "/**\n * Process ": "/**\n * 处理",
    "/**\n * Handle ": "/**\n * 处理",
    "/**\n * Record ": "/**\n * 记录",
    "/**\n * Export ": "/**\n * 导出",
    "/**\n * Import ": "/**\n * 导入",
    "/**\n * Analyze ": "/**\n * 分析",
    "/**\n * Detect ": "/**\n * 检测",
    "/**\n * Extract ": "/**\n * 提取",
    "/**\n * Parse ": "/**\n * 解析",
    "/**\n * Index ": "/**\n * 索引",
    "/**\n * Filter ": "/**\n * 过滤",
    "/**\n * Move ": "/**\n * 移动",
    "/**\n * Add ": "/**\n * 添加",
    "/**\n * Remove ": "/**\n * 移除",
    "/**\n * Reorder ": "/**\n * 重新排序",
    "/**\n * Compare ": "/**\n * 比较",
    "/**\n * Download ": "/**\n * 下载",
    
    # 行内注释
    "// Create ": "// 创建",
    "// Update ": "// 更新",
    "// Delete ": "// 删除",
    "// Get ": "// 获取",
    "// Find ": "// 查找",
    "// Search ": "// 搜索",
    "// Query ": "// 查询",
    "// Save ": "// 保存",
    "// Build ": "// 构建",
    "// Generate ": "// 生成",
    "// Validate ": "// 验证",
    "// Check ": "// 检查",
    "// Calculate ": "// 计算",
    "// Process ": "// 处理",
    "// Handle ": "// 处理",
    "// Record ": "// 记录",
    "// Set ": "// 设置",
    "// Add ": "// 添加",
    "// Remove ": "// 移除",
    "// Clear ": "// 清除",
    "// Log ": "// 记录日志",
    "// Return ": "// 返回",
    "// Convert ": "// 转换",
    "// Extract ": "// 提取",
    "// Parse ": "// 解析",
    "// Filter ": "// 过滤",
    "// Sort ": "// 排序",
    "// Map ": "// 映射",
    "// Apply ": "// 应用",
    "// Execute ": "// 执行",
    "// Invoke ": "// 调用",
    "// Call ": "// 调用",
    "// Send ": "// 发送",
    "// Receive ": "// 接收",
    "// Load ": "// 加载",
    "// Initialize ": "// 初始化",
    "// Configure ": "// 配置",
    "// Register ": "// 注册",
    "// Unregister ": "// 注销",
    "// Start ": "// 启动",
    "// Stop ": "// 停止",
    "// Close ": "// 关闭",
    "// Open ": "// 打开",
    "// Read ": "// 读取",
    "// Write ": "// 写入",
    "// Append ": "// 追加",
    "// Merge ": "// 合并",
    "// Split ": "// 分割",
    "// Join ": "// 连接",
    "// Combine ": "// 组合",
    "// Transform ": "// 转换",
    "// Format ": "// 格式化",
    "// Encode ": "// 编码",
    "// Decode ": "// 解码",
    "// Encrypt ": "// 加密",
    "// Decrypt ": "// 解密",
    "// Compress ": "// 压缩",
    "// Decompress ": "// 解压",
    "// Serialize ": "// 序列化",
    "// Deserialize ": "// 反序列化",
    
    # 常见短语
    " not found": " 不存在",
    " is required": " 是必需的",
    " is invalid": " 无效",
    " is empty": " 为空",
    " is null": " 为空",
    " already exists": " 已存在",
    " is disabled": " 已禁用",
    " is enabled": " 已启用",
    " is active": " 处于激活状态",
    " is inactive": " 处于非激活状态",
    " failed": " 失败",
    " successful": " 成功",
    " completed": " 完成",
    " pending": " 待处理",
    " running": " 运行中",
    " cancelled": " 已取消",
    " expired": " 已过期",
    " denied": " 被拒绝",
    " granted": " 已授予",
    " forbidden": " 禁止",
    " unauthorized": " 未授权",
    " authenticated": " 已认证",
    " unauthenticated": " 未认证",
    
    # 日志消息
    "log.info(\"": "log.info(\"",
    "log.warn(\"": "log.warn(\"",
    "log.error(\"": "log.error(\"",
    "log.debug(\"": "log.debug(\"",
    
    # 异常消息
    "throw new ": "throw new ",
    "throws ": "throws ",
    
    # 参数注释
    " * @param ": " * @param ",
    " * @return ": " * @return ",
    " * @throws ": " * @throws ",
    
    # 验证注释
    " * Validates: ": " * 验证需求: ",
    " * Requirement ": " * 需求 ",
    " * Requirements ": " * 需求 ",
}

# 更详细的翻译映射
DETAILED_TRANSLATIONS = {
    # 完整的日志消息
    'log.info("Creating ': 'log.info("创建',
    'log.info("Updating ': 'log.info("更新',
    'log.info("Deleting ': 'log.info("删除',
    'log.info("Getting ': 'log.info("获取',
    'log.info("Finding ': 'log.info("查找',
    'log.info("Searching ': 'log.info("搜索',
    'log.info("Querying ': 'log.info("查询',
    'log.info("Saving ': 'log.info("保存',
    'log.info("Building ': 'log.info("构建',
    'log.info("Generating ': 'log.info("生成',
    'log.info("Validating ': 'log.info("验证',
    'log.info("Checking ': 'log.info("检查',
    'log.info("Processing ': 'log.info("处理',
    'log.info("Handling ': 'log.info("处理',
    'log.info("Recording ': 'log.info("记录',
    'log.info("Exporting ': 'log.info("导出',
    'log.info("Importing ': 'log.info("导入',
    
    'log.warn("Creating ': 'log.warn("创建',
    'log.warn("Updating ': 'log.warn("更新',
    'log.warn("Deleting ': 'log.warn("删除',
    'log.warn("Getting ': 'log.warn("获取',
    'log.warn("Finding ': 'log.warn("查找',
    
    'log.error("Creating ': 'log.error("创建',
    'log.error("Updating ': 'log.error("更新',
    'log.error("Deleting ': 'log.error("删除',
    'log.error("Getting ': 'log.error("获取',
    'log.error("Finding ': 'log.error("查找',
    
    'log.debug("Creating ': 'log.debug("创建',
    'log.debug("Updating ': 'log.debug("更新',
    'log.debug("Deleting ': 'log.debug("删除',
    'log.debug("Getting ': 'log.debug("获取',
    'log.debug("Finding ': 'log.debug("查找',
    
    # 常见异常消息
    'throw new ResourceNotFoundException("': 'throw new ResourceNotFoundException("',
    'throw new BusinessException("': 'throw new BusinessException("',
    'throw new IllegalArgumentException("': 'throw new IllegalArgumentException("',
    'throw new IllegalStateException("': 'throw new IllegalStateException("',
    'throw new RuntimeException("': 'throw new RuntimeException("',
    
    # 常见单词
    "table": "表",
    "column": "字段",
    "database": "数据库",
    "user": "用户",
    "password": "密码",
    "token": "令牌",
    "lineage": "血缘关系",
    "catalog": "目录",
    "quality": "质量",
    "history": "历史",
    "search": "搜索",
    "export": "导出",
    "import": "导入",
    "cache": "缓存",
    "query": "查询",
    "request": "请求",
    "response": "响应",
    "error": "错误",
    "success": "成功",
    "failed": "失败",
    "permission": "权限",
    "role": "角色",
    "authentication": "认证",
    "authorization": "授权",
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
        
        for english, chinese in DETAILED_TRANSLATIONS.items():
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
    
    print(f"开始批量翻译 Java 文件...")
    print(f"目录: {backend_dir}")
    print("-" * 80)
    
    # 遍历所有 Java 文件
    java_files = list(backend_dir.rglob('*.java'))
    translated_count = 0
    
    for java_file in java_files:
        if translate_file(java_file):
            translated_count += 1
    
    print("-" * 80)
    print(f"完成! 共翻译 {translated_count}/{len(java_files)} 个文件")
    print(f"\n注意: 此脚本只进行基础翻译，建议手动检查以下内容：")
    print("  1. 复杂的注释段落")
    print("  2. 业务逻辑相关的注释")
    print("  3. 异常消息的完整性")
    print("  4. 日志消息的准确性")

if __name__ == '__main__':
    main()
