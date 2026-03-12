#!/usr/bin/env python3
"""
Fix UTF-8 encoding issues by removing Chinese comments from Java files
"""

import os
import re

# Files with UTF-8 encoding issues
files_to_fix = [
    "backend-java/src/main/java/com/kiro/metadata/dto/response/CatalogResponse.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/ChangeHistoryResponse.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/ColumnResponse.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/ExportStatusResponse.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/LineageEdge.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/LineageResponse.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/QualityMetricsResponse.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/TableResponse.java",
    "backend-java/src/main/java/com/kiro/metadata/dto/response/UserResponse.java",
]

def fix_file(filepath):
    """Remove lines with Chinese comments that cause UTF-8 encoding errors"""
    try:
        # Try different encodings
        content = None
        for encoding in ['utf-8', 'gbk', 'gb2312', 'gb18030', 'latin-1']:
            try:
                with open(filepath, 'r', encoding=encoding) as f:
                    content = f.read()
                break
            except:
                continue
        
        if content is None:
            print(f"✗ Could not read {filepath} with any encoding")
            return False
        
        lines = content.splitlines(keepends=True)
        fixed_lines = []
        for line in lines:
            # Skip lines that are pure Chinese comments
            if re.match(r'^\s*//\s*[\u4e00-\u9fff]+', line):
                continue
            # Skip lines with "从...转换" pattern (conversion comments)
            if '从' in line and '转换' in line:
                continue
            fixed_lines.append(line)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(fixed_lines)
        
        print(f"✓ Fixed: {filepath}")
        return True
    except Exception as e:
        print(f"✗ Error fixing {filepath}: {e}")
        return False

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    print("Fixing UTF-8 encoding issues in DTO files...")
    print("=" * 60)
    
    success_count = 0
    for file_path in files_to_fix:
        full_path = os.path.join(base_dir, file_path)
        if os.path.exists(full_path):
            if fix_file(full_path):
                success_count += 1
        else:
            print(f"✗ File not found: {full_path}")
    
    print("=" * 60)
    print(f"Fixed {success_count}/{len(files_to_fix)} files")

if __name__ == "__main__":
    main()
