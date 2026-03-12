#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复特定文件中的乱码注释
直接替换整个注释块
"""

import sys
from pathlib import Path

# 需要修复的文件和对应的正确注释
FILES_TO_FIX = {
    "dto/response/CatalogResponse.java": {
        "class_comment": """/**
 * 数据目录响应 DTO
 */""",
        "method_comment": """    /**
     * 从Catalog实体转换为CatalogResponse
     */"""
    },
    "dto/response/ColumnResponse.java": {
        "class_comment": """/**
 * 字段响应 DTO
 */""",
        "method_comment": """    /**
     * 从ColumnMetadata实体转换为ColumnResponse
     */"""
    },
    "dto/response/QualityMetricsResponse.java": {
        "class_comment": """/**
 * 数据质量指标响应 DTO
 */""",
        "method_comment": """    /**
     * 从QualityMetrics实体转换为QualityMetricsResponse
     */"""
    },
    "dto/response/TableResponse.java": {
        "class_comment": """/**
 * 表响应 DTO
 */""",
        "method_comment1": """    /**
     * 从TableMetadata实体转换为TableResponse
     */""",
        "method_comment2": """    /**
     * 从TableMetadata实体转换为TableResponse（不包含字段列表）
     */"""
    },
    "dto/response/ChangeHistoryResponse.java": {
        "class_comment": """/**
 * 变更历史响应 DTO
 */""",
        "method_comment": """    /**
     * 从ChangeHistory实体转换为ChangeHistoryResponse
     */"""
    },
    "dto/response/UserResponse.java": {
        "class_comment": """/**
 * 用户响应 DTO
 */""",
        "method_comment": """    /**
     * 从User实体转换为UserResponse
     */"""
    },
}


def fix_file(file_path: Path, comments: dict) -> bool:
    """修复单个文件"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        modified = False
        new_lines = []
        i = 0
        
        while i < len(lines):
            line = lines[i]
            
            # 检查是否是注释开始
            if line.strip() == '/**':
                # 收集整个注释块
                comment_block = [line]
                i += 1
                while i < len(lines) and '*/' not in lines[i]:
                    comment_block.append(lines[i])
                    i += 1
                if i < len(lines):
                    comment_block.append(lines[i])  # 添加 */
                
                # 检查注释内容是否包含乱码
                comment_text = ''.join(comment_block)
                has_garbled = any(ord(c) > 0x4E00 and ord(c) < 0x9FFF for c in comment_text if c not in '从实体转换为')
                
                if has_garbled:
                    # 判断是类注释还是方法注释
                    # 查看注释后的几行
                    next_lines = ''.join(lines[i+1:min(i+5, len(lines))])
                    
                    if '@Data' in next_lines or 'public class' in next_lines:
                        # 类注释
                        new_lines.append(comments['class_comment'] + '\n')
                        modified = True
                    elif 'public static' in next_lines and 'from(' in next_lines:
                        # 方法注释
                        if 'method_comment' in comments:
                            new_lines.append(comments['method_comment'] + '\n')
                            modified = True
                        elif 'method_comment1' in comments and 'WithoutColumns' not in next_lines:
                            new_lines.append(comments['method_comment1'] + '\n')
                            modified = True
                        elif 'method_comment2' in comments and 'WithoutColumns' in next_lines:
                            new_lines.append(comments['method_comment2'] + '\n')
                            modified = True
                        else:
                            new_lines.extend(comment_block)
                    else:
                        new_lines.extend(comment_block)
                else:
                    new_lines.extend(comment_block)
                
                i += 1
            else:
                new_lines.append(line)
                i += 1
        
        if modified:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
            return True
        
        return False
        
    except Exception as e:
        print(f"错误: {e}")
        return False


def main():
    """主函数"""
    script_dir = Path(__file__).parent
    backend_dir = script_dir.parent / 'backend-java' / 'src' / 'main' / 'java' / 'com' / 'kiro' / 'metadata'
    
    if not backend_dir.exists():
        print(f"错误：目录不存在 {backend_dir}")
        sys.exit(1)
    
    print("=" * 70)
    print("修复乱码注释工具")
    print("=" * 70)
    print()
    
    fixed_count = 0
    
    for relative_path, comments in FILES_TO_FIX.items():
        file_path = backend_dir / relative_path
        if not file_path.exists():
            print(f"⚠️  文件不存在: {relative_path}")
            continue
        
        print(f"处理: {relative_path} ... ", end='')
        if fix_file(file_path, comments):
            print("✅")
            fixed_count += 1
        else:
            print("⏭️")
    
    print()
    print("=" * 70)
    print(f"修复完成：修改 {fixed_count} 个文件")
    print("=" * 70)
    
    if fixed_count > 0:
        print("\n✅ 修复完成！")
        print("\n下一步：")
        print("  1. 编译验证：cd backend-java && mvn clean compile")
        print("  2. 如果成功：git add . && git commit -m \"修复中文注释乱码\"")


if __name__ == '__main__':
    main()
