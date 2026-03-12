#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
最终翻译工具 - 只翻译 Javadoc 注释和异常消息
使用 AI 辅助翻译，确保准确性
"""

import re
import sys
from pathlib import Path
from typing import Tuple, Dict

# 翻译映射表
COMMENT_TRANSLATIONS = {
    # Entity 相关
    "User entity": "用户实体",
    "Represents a user in the metadata management system": "表示元数据管理系统中的用户",
    "Table metadata entity": "表元数据实体",
    "Column metadata entity": "字段元数据实体",
    "Lineage entity": "血缘关系实体",
    "Represents a lineage relationship between two tables": "表示两个表之间的血缘关系",
    "Lineage relationship entity": "血缘关系实体",
    "Catalog entity": "目录实体",
    "Quality metrics entity": "质量指标实体",
    "Change history entity": "变更历史实体",
    "Export task entity": "导出任务实体",
    
    # Enum 相关
    "Export type enumeration": "导出类型枚举",
    "Defines the export file formats": "定义导出文件格式",
    "Task status enumeration": "任务状态枚举",
    "Defines the status of export tasks": "定义导出任务的状态",
    "CSV format": "CSV格式",
    "JSON format": "JSON格式",
    "Task is pending execution": "任务等待执行",
    "Task is currently running": "任务正在运行",
    "Task completed successfully": "任务成功完成",
    "Task failed with error": "任务失败并出错",
    
    # Exception 相关
    "Resource not found exception": "资源未找到异常",
    "Circular dependency exception": "循环依赖异常",
    "Duplicate resource exception": "资源重复异常",
    "Forbidden exception": "禁止访问异常",
    "Unauthorized exception": "未授权异常",
    "Max level exceeded exception": "超出最大层级异常",
    
    # Role 相关
    "Administrator role": "管理员角色",
    "Developer role": "开发者角色",
    "Guest role": "访客角色",
    
    # Operation 相关
    "Create operation": "创建操作",
    "Update operation": "更新操作",
    "Delete operation": "删除操作",
    
    # Status 相关
    "Pending status": "等待状态",
    "Running status": "运行状态",
    "Completed status": "完成状态",
    "Failed status": "失败状态",
    
    # Export 相关
    "CSV export": "CSV导出",
    "JSON export": "JSON导出",
    
    # Table Type 相关
    "Regular table": "普通表",
    "View table": "视图表",
    "External table": "外部表",
    
    # Lineage Type 相关
    "Direct lineage": "直接血缘",
    "Indirect lineage": "间接血缘",
    
    # Service 相关
    "JWT token provider": "JWT令牌提供者",
    "User details service implementation": "用户详情服务实现",
    "Authentication service": "认证服务",
    "Catalog service": "目录服务",
    "History service": "历史服务",
    "Metadata service": "元数据服务",
    "Search service": "搜索服务",
    "SQL parser service": "SQL解析服务",
    "Column service": "字段服务",
    "Lineage service": "血缘关系服务",
    "Quality service": "质量服务",
    "Import/Export service": "导入导出服务",
    
    # Config/Aspect 相关
    "Permission aspect": "权限切面",
    "Require specific role": "需要特定角色",
    "Security configuration": "安全配置",
    "Web MVC configuration": "Web MVC配置",
    
    # Repository 相关
    "Export task repository": "导出任务仓库",
    "Table repository": "表仓库",
    "User repository": "用户仓库",
    "Catalog repository": "目录仓库",
    "Change history repository": "变更历史仓库",
    
    # Request/Response 相关
    "Search request": "搜索请求",
    "Table create request": "表创建请求",
    "Table update request": "表更新请求",
    "Column create request": "字段创建请求",
    "Column update request": "字段更新请求",
    "Reorder columns request": "字段重排序请求",
    "Login request": "登录请求",
    "Refresh token request": "刷新令牌请求",
    "Export status response": "导出状态响应",
    "Table response": "表响应",
    "Column response": "字段响应",
    "User response": "用户响应",
    "Lineage response": "血缘关系响应",
    "Lineage graph": "血缘关系图",
    "Change history response": "变更历史响应",
    "Paged response": "分页响应",
    "Import result response": "导入结果响应",
    "Token response": "令牌响应",
    
    # Controller 相关
    "Authentication controller": "认证控制器",
    "Table controller": "表控制器",
    "Column controller": "字段控制器",
    "Catalog controller": "目录控制器",
    "Lineage controller": "血缘关系控制器",
    "History controller": "历史控制器",
    "Quality controller": "质量控制器",
    "Search controller": "搜索控制器",
    "Import/Export controller": "导入导出控制器",
    
    # Security 相关
    "JWT authentication filter": "JWT认证过滤器",
    "JWT authentication entry point": "JWT认证入口点",
    "JWT access denied handler": "JWT访问拒绝处理器",
    "User details service": "用户详情服务",
    
    # 其他常见短语
    "Handles": "处理",
    "Provides": "提供",
    "Manages": "管理",
    "Validates": "验证",
    "Processes": "处理",
    "Returns": "返回",
    "Creates": "创建",
    "Updates": "更新",
    "Deletes": "删除",
    "Retrieves": "检索",
    "Searches": "搜索",
    "Filters": "过滤",
    "Sorts": "排序",
}

# 异常消息翻译
EXCEPTION_MESSAGES = {
    "not found": "未找到",
    "already exists": "已存在",
    "is required": "是必填项",
    "is invalid": "无效",
    "access denied": "访问被拒绝",
    "unauthorized": "未授权",
    "forbidden": "禁止访问",
    "circular dependency detected": "检测到循环依赖",
    "max level exceeded": "超出最大层级",
    "does not exist": "不存在",
    "cannot be null": "不能为空",
    "cannot be empty": "不能为空",
    "invalid format": "格式无效",
    "invalid value": "值无效",
    "operation failed": "操作失败",
    "permission denied": "权限被拒绝",
    "authentication failed": "认证失败",
    "token expired": "令牌已过期",
    "invalid token": "无效的令牌",
    "user not found": "用户未找到",
    "table not found": "表未找到",
    "column not found": "字段未找到",
    "catalog not found": "目录未找到",
    "lineage not found": "血缘关系未找到",
}


def translate_javadoc(content: str) -> str:
    """翻译 Javadoc 注释"""
    result = content
    for en, zh in COMMENT_TRANSLATIONS.items():
        # 不区分大小写的替换
        pattern = re.compile(re.escape(en), re.IGNORECASE)
        result = pattern.sub(zh, result)
    return result


def translate_exception_message(content: str) -> str:
    """翻译异常消息"""
    result = content
    for en, zh in EXCEPTION_MESSAGES.items():
        pattern = re.compile(re.escape(en), re.IGNORECASE)
        result = pattern.sub(zh, result)
    return result


def process_java_file(file_path: Path) -> Tuple[bool, str]:
    """
    处理 Java 文件
    返回: (是否修改, 错误信息)
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 1. 翻译 Javadoc 注释 (/** ... */)
        def replace_javadoc(match):
            javadoc = match.group(0)
            translated = translate_javadoc(javadoc)
            return translated
        
        content = re.sub(r'/\*\*.*?\*/', replace_javadoc, content, flags=re.DOTALL)
        
        # 2. 翻译异常消息中的字符串
        # 匹配 throw new XxxException("message")
        def replace_exception(match):
            full_match = match.group(0)
            message = match.group(1)
            translated_message = translate_exception_message(message)
            return full_match.replace(f'"{message}"', f'"{translated_message}"')
        
        content = re.sub(
            r'throw\s+new\s+\w+Exception\s*\(\s*"([^"]+)"\s*\)',
            replace_exception,
            content
        )
        
        # 只有内容真的改变了才写入
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True, ""
        
        return False, ""
        
    except Exception as e:
        return False, str(e)


def main():
    """主函数"""
    script_dir = Path(__file__).parent
    backend_dir = script_dir.parent / 'backend-java' / 'src' / 'main' / 'java'
    
    if not backend_dir.exists():
        print(f"错误：目录不存在 {backend_dir}")
        sys.exit(1)
    
    print("=" * 70)
    print("Java 代码注释和异常消息翻译工具")
    print("=" * 70)
    print(f"目标目录：{backend_dir}")
    print()
    print("翻译内容：")
    print("  ✓ Javadoc 注释 (/** ... */)")
    print("  ✓ 异常消息字符串")
    print("  ✗ 不翻译代码")
    print("  ✗ 不翻译单行注释")
    print("=" * 70)
    print()
    
    # 查找所有 Java 文件
    java_files = list(backend_dir.rglob('*.java'))
    print(f"找到 {len(java_files)} 个 Java 文件\n")
    
    modified_count = 0
    error_count = 0
    
    for i, java_file in enumerate(java_files, 1):
        relative_path = java_file.relative_to(backend_dir)
        print(f"[{i:2d}/{len(java_files)}] {str(relative_path):60s} ", end='')
        
        modified, error = process_java_file(java_file)
        
        if error:
            print(f"❌ {error}")
            error_count += 1
        elif modified:
            print("✅")
            modified_count += 1
        else:
            print("⏭️")
    
    print()
    print("=" * 70)
    print(f"翻译完成：修改 {modified_count} 个文件，{error_count} 个错误")
    print("=" * 70)
    
    if error_count > 0:
        print("\n⚠️  存在错误，请检查")
        sys.exit(1)
    
    print("\n✅ 翻译完成！")
    print("\n下一步：")
    print("  1. 编译验证：cd backend-java && mvn clean compile")
    print("  2. 如果失败：git checkout .")
    print("  3. 如果成功：git add . && git commit -m \"翻译代码注释和异常消息为中文\"")


if __name__ == '__main__':
    main()
