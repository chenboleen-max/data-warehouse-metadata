#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
智能代码翻译脚本 - 只翻译注释和字符串消息
安全规则：
1. 不翻译 Java 关键字
2. 不翻译类名、方法名、变量名
3. 只翻译注释内容
4. 只翻译异常消息和日志消息
"""

import os
import re
import sys
from pathlib import Path
from typing import Dict, List, Tuple

# 翻译词典 - 只用于注释和消息
TRANSLATIONS = {
    # 实体和概念
    "User entity": "用户实体",
    "Table metadata entity": "表元数据实体",
    "Column metadata entity": "字段元数据实体",
    "Lineage relationship entity": "血缘关系实体",
    "Catalog entity": "目录实体",
    "Quality metrics entity": "质量指标实体",
    "Change history entity": "变更历史实体",
    "Export task entity": "导出任务实体",
    
    # 常见短语
    "not found": "未找到",
    "already exists": "已存在",
    "is required": "是必填项",
    "is invalid": "无效",
    "access denied": "访问被拒绝",
    "unauthorized": "未授权",
    "forbidden": "禁止访问",
    "internal server error": "内部服务器错误",
    "bad request": "错误的请求",
    
    # 操作描述
    "Create": "创建",
    "Update": "更新",
    "Delete": "删除",
    "Query": "查询",
    "Search": "搜索",
    "Filter": "过滤",
    "Sort": "排序",
    "Export": "导出",
    "Import": "导入",
    
    # 状态
    "Success": "成功",
    "Failed": "失败",
    "Pending": "等待中",
    "Running": "运行中",
    "Completed": "已完成",
    
    # 字段描述
    "username": "用户名",
    "password": "密码",
    "email": "邮箱",
    "role": "角色",
    "database name": "数据库名",
    "table name": "表名",
    "column name": "字段名",
    "description": "描述",
    "created at": "创建时间",
    "updated at": "更新时间",
}


class JavaTranslator:
    """Java 代码翻译器"""
    
    def __init__(self):
        self.java_keywords = {
            'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch',
            'char', 'class', 'const', 'continue', 'default', 'do', 'double',
            'else', 'enum', 'extends', 'final', 'finally', 'float', 'for',
            'goto', 'if', 'implements', 'import', 'instanceof', 'int',
            'interface', 'long', 'native', 'new', 'package', 'private',
            'protected', 'public', 'return', 'short', 'static', 'strictfp',
            'super', 'switch', 'synchronized', 'this', 'throw', 'throws',
            'transient', 'try', 'void', 'volatile', 'while', 'true', 'false', 'null'
        }
    
    def translate_comment(self, comment: str) -> str:
        """翻译注释内容"""
        result = comment
        for en, zh in TRANSLATIONS.items():
            # 不区分大小写的替换
            pattern = re.compile(re.escape(en), re.IGNORECASE)
            result = pattern.sub(zh, result)
        return result
    
    def translate_string_literal(self, text: str) -> str:
        """翻译字符串字面量"""
        result = text
        for en, zh in TRANSLATIONS.items():
            pattern = re.compile(re.escape(en), re.IGNORECASE)
            result = pattern.sub(zh, result)
        return result
    
    def process_line(self, line: str) -> str:
        """处理单行代码"""
        # 1. 处理单行注释 //
        if '//' in line:
            code_part, comment_part = line.split('//', 1)
            translated_comment = self.translate_comment(comment_part)
            return code_part + '//' + translated_comment
        
        # 2. 处理行内多行注释 /* ... */
        if '/*' in line and '*/' in line:
            match = re.search(r'/\*(.+?)\*/', line)
            if match:
                comment = match.group(1)
                translated = self.translate_comment(comment)
                return line.replace(comment, translated)
        
        # 3. 处理异常消息 throw new XxxException("message")
        throw_pattern = r'throw\s+new\s+\w+Exception\s*\(\s*"([^"]+)"\s*\)'
        match = re.search(throw_pattern, line)
        if match:
            message = match.group(1)
            translated = self.translate_string_literal(message)
            return line.replace(f'"{message}"', f'"{translated}"')
        
        # 4. 处理日志消息 log.xxx("message")
        log_pattern = r'log\.\w+\s*\(\s*"([^"{}]+)"\s*\)'
        match = re.search(log_pattern, line)
        if match:
            message = match.group(1)
            translated = self.translate_string_literal(message)
            return line.replace(f'"{message}"', f'"{translated}"')
        
        return line
    
    def translate_file(self, file_path: Path) -> Tuple[bool, str]:
        """
        翻译 Java 文件
        返回: (是否修改, 错误信息)
        """
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            
            new_lines = []
            in_multiline_comment = False
            multiline_comment_lines = []
            
            for line in lines:
                stripped = line.strip()
                
                # 检测多行注释开始
                if '/*' in stripped and '*/' not in stripped:
                    in_multiline_comment = True
                    multiline_comment_lines = [line]
                    continue
                
                # 在多行注释中
                if in_multiline_comment:
                    multiline_comment_lines.append(line)
                    if '*/' in stripped:
                        # 多行注释结束，翻译整个注释块
                        comment_block = ''.join(multiline_comment_lines)
                        # 提取注释内容（去掉 /* 和 */）
                        content_match = re.search(r'/\*(.*?)\*/', comment_block, re.DOTALL)
                        if content_match:
                            content = content_match.group(1)
                            translated_content = self.translate_comment(content)
                            translated_block = comment_block.replace(content, translated_content)
                            new_lines.append(translated_block)
                        else:
                            new_lines.extend(multiline_comment_lines)
                        in_multiline_comment = False
                        multiline_comment_lines = []
                    continue
                
                # 处理普通行
                new_line = self.process_line(line)
                new_lines.append(new_line)
            
            # 写回文件
            new_content = ''.join(new_lines)
            with open(file_path, 'r', encoding='utf-8') as f:
                original_content = f.read()
            
            if new_content != original_content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
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
    print("智能代码翻译工具")
    print("=" * 70)
    print(f"目标目录：{backend_dir}")
    print()
    print("翻译规则：")
    print("  ✓ 翻译注释内容（// 和 /* */）")
    print("  ✓ 翻译异常消息（throw new Exception(\"...\")）")
    print("  ✓ 翻译日志消息（log.xxx(\"...\")）")
    print("  ✗ 不翻译类名、方法名、变量名")
    print("  ✗ 不翻译 Java 关键字")
    print("=" * 70)
    print()
    
    # 查找所有 Java 文件
    java_files = list(backend_dir.rglob('*.java'))
    print(f"找到 {len(java_files)} 个 Java 文件")
    print()
    
    translator = JavaTranslator()
    modified_count = 0
    error_count = 0
    
    for i, java_file in enumerate(java_files, 1):
        relative_path = java_file.relative_to(backend_dir)
        print(f"[{i}/{len(java_files)}] 处理: {relative_path}...", end=' ')
        
        modified, error = translator.translate_file(java_file)
        
        if error:
            print(f"❌ 错误: {error}")
            error_count += 1
        elif modified:
            print("✅ 已翻译")
            modified_count += 1
        else:
            print("⏭️  无需修改")
    
    print()
    print("=" * 70)
    print("翻译完成！")
    print(f"  修改文件数：{modified_count}")
    print(f"  错误文件数：{error_count}")
    print(f"  总文件数：{len(java_files)}")
    print("=" * 70)
    
    if error_count > 0:
        print("\n⚠️  存在错误，请检查上述错误信息")
        sys.exit(1)
    
    print("\n✅ 所有文件处理完成！")
    print("\n下一步：")
    print("  1. 运行编译测试：cd backend-java && mvn clean compile")
    print("  2. 如果编译失败，运行：git checkout .")
    print("  3. 如果编译成功，提交更改：git add . && git commit -m \"翻译代码注释和消息为中文\"")


if __name__ == '__main__':
    main()
