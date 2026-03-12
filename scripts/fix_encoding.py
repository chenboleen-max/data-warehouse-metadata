#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复 Java 文件中的编码乱码问题
将错误编码的中文字符转换为正确的 UTF-8 编码
"""

import re
import sys
from pathlib import Path
from typing import Tuple

# 乱码到正确中文的映射
ENCODING_FIX_MAP = {
    # Response DTO 相关
    "鏁版嵁鐩綍鍝嶅簲": "数据目录响应",
    "鏁版嵁璐ㄩ噺鎸囨爣鍝嶅簲": "数据质量指标响应",
    
    # 转换方法相关
    "浠?": "从",
    "瀹炰綋": "实体",
    "杞崲涓?": "转换为",
    "锛堜笉鍖呭惈瀛楁鍒楄〃锛?": "（不包含字段列表）",
    
    # 完整的转换短语
    "从Catalog 实体杞崲涓?CatalogResponse": "从Catalog 实体转换为CatalogResponse",
    "从ColumnMetadata 实体杞崲涓?ColumnResponse": "从ColumnMetadata 实体转换为ColumnResponse",
    "从QualityMetrics 实体杞崲涓?QualityMetricsResponse": "从QualityMetrics 实体转换为QualityMetricsResponse",
    "从TableMetadata 实体杞崲涓?TableResponse": "从TableMetadata 实体转换为TableResponse",
    "从TableMetadata 实体杞崲涓?TableResponse锛堜笉鍖呭惈瀛楁鍒楄〃锛?": "从TableMetadata 实体转换为TableResponse（不包含字段列表）",
    "从ChangeHistory 实体杞崲涓?ChangeHistoryResponse": "从ChangeHistory 实体转换为ChangeHistoryResponse",
    "从User 实体杞崲涓?UserResponse": "从User 实体转换为UserResponse",
    
    # 实体名称
    "ColumnMetadata": "ColumnMetadata",
    "QualityMetrics": "QualityMetrics",
    "TableMetadata": "TableMetadata",
    "ChangeHistory": "ChangeHistory",
    "Catalog": "Catalog",
    "User": "User",
    
    # Response 类名
    "ColumnResponse": "ColumnResponse",
    "QualityMetricsResponse": "QualityMetricsResponse",
    "TableResponse": "TableResponse",
    "ChangeHistoryResponse": "ChangeHistoryResponse",
    "CatalogResponse": "CatalogResponse",
    "UserResponse": "UserResponse",
}


def fix_encoding(content: str) -> str:
    """修复编码问题"""
    result = content
    for garbled, correct in ENCODING_FIX_MAP.items():
        result = result.replace(garbled, correct)
    return result


def process_java_file(file_path: Path) -> Tuple[bool, str]:
    """
    处理 Java 文件
    返回: (是否修改, 错误信息)
    """
    try:
        # 尝试用 UTF-8 读取
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 修复编码
        content = fix_encoding(content)
        
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
    print("Java 文件编码乱码修复工具")
    print("=" * 70)
    print(f"目标目录：{backend_dir}")
    print()
    print("修复内容：")
    print("  ✓ 修复错误编码的中文字符")
    print("  ✓ 转换为正确的 UTF-8 编码")
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
    print(f"修复完成：修改 {modified_count} 个文件，{error_count} 个错误")
    print("=" * 70)
    
    if error_count > 0:
        print("\n⚠️  存在错误，请检查")
        sys.exit(1)
    
    print("\n✅ 修复完成！")
    print("\n下一步：")
    print("  1. 编译验证：cd backend-java && mvn clean compile")
    print("  2. 如果失败：git checkout .")
    print("  3. 如果成功：git add . && git commit -m \"修复中文注释编码乱码\"")


if __name__ == '__main__':
    main()
