#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
安全的代码注释和字符串翻译脚本
只翻译注释和字符串字面量，不修改代码结构
"""

import os
import re
import sys
from pathlib import Path
from typing import List, Tuple

# 简单的英译中词典（常见编程术语）
TRANSLATION_DICT = {
    # 通用术语
    "User": "用户",
    "Table": "表",
    "Column": "字段",
    "Database": "数据库",
    "Metadata": "元数据",
    "Lineage": "血缘关系",
    "Catalog": "目录",
    "Quality": "质量",
    "Metrics": "指标",
    "History": "历史",
    "Change": "变更",
    "Export": "导出",
    "Import": "导入",
    "Search": "搜索",
    "Filter": "过滤",
    "Sort": "排序",
    "Page": "分页",
    
    # 操作
    "Create": "创建",
    "Update": "更新",
    "Delete": "删除",
    "Query": "查询",
    "Find": "查找",
    "Get": "获取",
    "Set": "设置",
    "Add": "添加",
    "Remove": "移除",
    "Save": "保存",
    "Load": "加载",
    
    # 状态和结果
    "Success": "成功",
    "Failed": "失败",
    "Error": "错误",
    "Warning": "警告",
    "Info": "信息",
    "Not found": "未找到",
    "Already exists": "已存在",
    "Invalid": "无效",
    "Required": "必填",
    "Optional": "可选",
    
    # 权限和认证
    "Authentication": "认证",
    "Authorization": "授权",
    "Permission": "权限",
    "Access denied": "访问被拒绝",
    "Unauthorized": "未授权",
    "Forbidden": "禁止访问",
    "Token": "令牌",
    "Login": "登录",
    "Logout": "登出",
    
    # 数据相关
    "Record": "记录",
    "Field": "字段",
    "Value": "值",
    "Type": "类型",
    "Format": "格式",
    "Size": "大小",
    "Count": "数量",
    "Total": "总计",
    "Empty": "空",
    "Null": "空值",
    
    # 时间相关
    "Created": "创建时间",
    "Updated": "更新时间",
    "Deleted": "删除时间",
    "Timestamp": "时间戳",
    "Date": "日期",
    "Time": "时间",
    
    # 关系
    "Parent": "父级",
    "Child": "子级",
    "Source": "源",
    "Target": "目标",
    "Upstream": "上游",
    "Downstream": "下游",
    "Dependency": "依赖",
    "Circular": "循环",
    
    # 其他
    "Request": "请求",
    "Response": "响应",
    "Status": "状态",
    "Message": "消息",
    "Description": "描述",
    "Name": "名称",
    "ID": "标识符",
    "Path": "路径",
    "Level": "层级",
}


def translate_text(text: str) -> str:
    """
    翻译文本（简单的词典替换）
    """
    result = text
    for en, zh in TRANSLATION_DICT.items():
        # 使用单词边界匹配，避免部分替换
        pattern = r'\b' + re.escape(en) + r'\b'
        result = re.sub(pattern, zh, result, flags=re.IGNORECASE)
    return result


def translate_java_file(file_path: Path) -> Tuple[bool, str]:
    """
    翻译 Java 文件中的注释和字符串字面量
    
    返回: (是否修改, 错误信息)
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        lines = content.split('\n')
        new_lines = []
        
        in_multiline_comment = False
        
        for line in lines:
            new_line = line
            
            # 检查是否在多行注释中
            if '/*' in line and '*/' not in line:
                in_multiline_comment = True
            elif '*/' in line:
                in_multiline_comment = False
            
            # 翻译多行注释
            if in_multiline_comment or ('/*' in line and '*/' in line):
                # 提取注释内容
                comment_match = re.search(r'/\*(.+?)\*/', line)
                if comment_match:
                    comment_text = comment_match.group(1)
                    translated = translate_text(comment_text)
                    new_line = line.replace(comment_text, translated)
            
            # 翻译单行注释 //
            if '//' in line:
                parts = line.split('//', 1)
                if len(parts) == 2:
                    code_part = parts[0]
                    comment_part = parts[1]
                    translated_comment = translate_text(comment_part)
                    new_line = code_part + '//' + translated_comment
            
            # 翻译字符串字面量（只翻译异常消息和日志消息）
            # 匹配 throw new Exception("message")
            throw_match = re.search(r'throw\s+new\s+\w+Exception\("([^"]+)"\)', line)
            if throw_match:
                message = throw_match.group(1)
                translated_message = translate_text(message)
                new_line = new_line.replace(f'"{message}"', f'"{translated_message}"')
            
            # 匹配 log.xxx("message")
            log_match = re.search(r'log\.\w+\("([^"]+)"', line)
            if log_match:
                message = log_match.group(1)
                # 不翻译包含 {} 的日志模板
                if '{}' not in message and '{' not in message:
                    translated_message = translate_text(message)
                    new_line = new_line.replace(f'"{message}"', f'"{translated_message}"')
            
            new_lines.append(new_line)
        
        new_content = '\n'.join(new_lines)
        
        # 只有内容真的改变了才写入
        if new_content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            return True, ""
        
        return False, ""
        
    except Exception as e:
        return False, str(e)


def main():
    """主函数"""
    # 获取 backend-java 目录
    script_dir = Path(__file__).parent
    backend_dir = script_dir.parent / 'backend-java' / 'src' / 'main' / 'java'
    
    if not backend_dir.exists():
        print(f"错误：目录不存在 {backend_dir}")
        sys.exit(1)
    
    print(f"开始翻译 Java 文件...")
    print(f"目录：{backend_dir}")
    print("=" * 60)
    
    # 查找所有 Java 文件
    java_files = list(backend_dir.rglob('*.java'))
    print(f"找到 {len(java_files)} 个 Java 文件")
    print()
    
    modified_count = 0
    error_count = 0
    
    for java_file in java_files:
        relative_path = java_file.relative_to(backend_dir)
        modified, error = translate_java_file(java_file)
        
        if error:
            print(f"❌ {relative_path}: {error}")
            error_count += 1
        elif modified:
            print(f"✅ {relative_path}")
            modified_count += 1
        else:
            print(f"⏭️  {relative_path} (无需修改)")
    
    print()
    print("=" * 60)
    print(f"翻译完成！")
    print(f"  修改文件数：{modified_count}")
    print(f"  错误文件数：{error_count}")
    print(f"  总文件数：{len(java_files)}")
    
    if error_count > 0:
        sys.exit(1)


if __name__ == '__main__':
    main()
