#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
安全的注释翻译脚本 - 只翻译单行注释
避免处理多行注释以防止破坏代码结构
"""

import re
import sys
from pathlib import Path
from typing import Tuple

# 翻译词典
TRANSLATIONS = {
    # 实体描述
    "User entity": "用户实体",
    "Table metadata entity": "表元数据实体",
    "Column metadata entity": "字段元数据实体",
    "Lineage relationship entity": "血缘关系实体",
    "Catalog entity": "目录实体",
    "Quality metrics entity": "质量指标实体",
    "Change history entity": "变更历史实体",
    "Export task entity": "导出任务实体",
    
    # 角色和权限
    "Administrator role": "管理员角色",
    "Developer role": "开发者角色",
    "Guest role": "访客角色",
    "Permission aspect": "权限切面",
    "Require specific role": "需要特定角色",
    "Access denied": "访问被拒绝",
    
    # 操作类型
    "Create operation": "创建操作",
    "Update operation": "更新操作",
    "Delete operation": "删除操作",
    
    # 任务状态
    "Pending status": "等待状态",
    "Running status": "运行状态",
    "Completed status": "完成状态",
    "Failed status": "失败状态",
    
    # 导出类型
    "CSV export": "CSV导出",
    "JSON export": "JSON导出",
    
    # 表类型
    "Regular table": "普通表",
    "View table": "视图表",
    "External table": "外部表",
    
    # 血缘类型
    "Direct lineage": "直接血缘",
    "Indirect lineage": "间接血缘",
    
    # Repository
    "Export task repository": "导出任务仓库",
    "Table repository": "表仓库",
    "User repository": "用户仓库",
    
    # Service
    "JWT token provider": "JWT令牌提供者",
    "User details service": "用户详情服务",
    "Authentication service": "认证服务",
    "Catalog service": "目录服务",
    "History service": "历史服务",
    "Metadata service": "元数据服务",
    "Search service": "搜索服务",
    "SQL parser service": "SQL解析服务",
    
    # Request/Response
    "Search request": "搜索请求",
    "Table create request": "表创建请求",
    "Table update request": "表更新请求",
    "Export status response": "导出状态响应",
    
    # 常见短语
    "not found": "未找到",
    "already exists": "已存在",
    "is required": "是必填项",
    "is invalid": "无效",
    "Resource not found exception": "资源未找到异常",
}


def translate_single_line_comment(line: str) -> str:
    """只翻译单行注释 //"""
    if '//' not in line:
        return line
    
    # 分割代码和注释
    parts = line.split('//', 1)
    if len(parts) != 2:
        return line
    
    code_part = parts[0]
    comment_part = parts[1]
    
    # 翻译注释部分
    translated_comment = comment_part
    for en, zh in TRANSLATIONS.items():
        pattern = re.compile(re.escape(en), re.IGNORECASE)
        translated_comment = pattern.sub(zh, translated_comment)
    
    return code_part + '//' + translated_comment


def translate_file(file_path: Path) -> Tuple[bool, str]:
    """
    翻译 Java 文件 - 只处理单行注释
    返回: (是否修改, 错误信息)
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        new_lines = []
        modified = False
        
        for line in lines:
            new_line = translate_single_line_comment(line)
            if new_line != line:
                modified = True
            new_lines.append(new_line)
        
        if modified:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
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
    print("安全注释翻译工具（只翻译单行注释 //）")
    print("=" * 70)
    print(f"目标目录：{backend_dir}")
    print()
    print("翻译规则：")
    print("  ✓ 只翻译单行注释（// ...）")
    print("  ✗ 不翻译多行注释（/* ... */）")
    print("  ✗ 不翻译代码")
    print("  ✗ 不翻译字符串")
    print("=" * 70)
    print()
    
    # 查找所有 Java 文件
    java_files = list(backend_dir.rglob('*.java'))
    print(f"找到 {len(java_files)} 个 Java 文件")
    print()
    
    modified_count = 0
    error_count = 0
    
    for i, java_file in enumerate(java_files, 1):
        relative_path = java_file.relative_to(backend_dir)
        print(f"[{i}/{len(java_files)}] {relative_path}...", end=' ')
        
        modified, error = translate_file(java_file)
        
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
    print(f"修改：{modified_count} | 错误：{error_count} | 总计：{len(java_files)}")
    print("=" * 70)
    
    if error_count > 0:
        sys.exit(1)
    
    print("\n✅ 翻译完成！")
    print("\n下一步：")
    print("  1. 编译测试：cd backend-java && mvn clean compile")
    print("  2. 如果失败：git checkout .")


if __name__ == '__main__':
    main()
