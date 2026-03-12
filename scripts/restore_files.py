#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
恢复被错误翻译的 Java 文件
将被翻译的 Java 关键字和包名恢复为英文
"""

import os
import re
from pathlib import Path

# 需要恢复的映射（中文 -> 英文）
RESTORE_MAPPINGS = {
    # Java 关键字
    "导入 ": "import ",
    
    # 包名中的中文（必须在类名之前处理）
    ".密码": ".password",
    ".用户details": ".userdetails",
    ".查询_dsl": ".query_dsl",
    ".查询": ".query",
    ".响应s": ".responses",
    ".表": ".table",
    "令牌": "token",
    "组件s": "Components",
    
    # 注解属性名
    "字段Names": "columnNames",
    "字段List": "columnList",
    "字段Name": "columnName",
    "响应Code": "responseCode",
    
    # 常见中文词汇
    "用户": "user",
    "密码": "password",
    "数据库": "database",
    "表": "table",
    "字段": "column",
    "搜索": "search",
    "请求": "request",
    "响应": "response",
    "质量": "quality",
    "成功ly": "successfully",
    "完成": "completed",
    "不存在": "not found",
    "被拒绝": "denied",
    "认证": "authentication",
    "授权": "authorization",
    
    # 表名和字段名（实体类中的）
    "users": "users",
    "username": "username",
    "role": "role",
    "password_hash": "password_hash",
    "passwordHash": "passwordHash",
    
    # 类名后缀
    "Config": "Config",
    "Controller": "Controller",
    "Service": "Service",
    "Repository": "Repository",
    "Exception": "Exception",
    "Handler": "Handler",
    "Filter": "Filter",
    "Interceptor": "Interceptor",
    "Aspect": "Aspect",
    "Impl": "Impl",
    
    # 类名中的中文
    "控制器": "Controller",
    "服务": "Service",
    "仓库": "Repository",
    "异常": "Exception",
    "处理器": "Handler",
    "过滤器": "Filter",
    "拦截器": "Interceptor",
    "切面": "Aspect",
    "配置": "Config",
    
    # 包名中的中文
    "elastic搜索": "elasticsearch",
    "Elastic搜索": "Elasticsearch",
    
    # 常见包名中的单词
    ".缓存.": ".cache.",
    ".认证.": ".authentication.",
    ".授权.": ".authorization.",
    ".服务.": ".service.",
    ".控制器.": ".controller.",
    ".仓库.": ".repository.",
    ".配置.": ".config.",
    ".安全.": ".security.",
    ".实体.": ".entity.",
    ".异常.": ".exception.",
    ".拦截器.": ".interceptor.",
    ".过滤器.": ".filter.",
    ".组件.": ".component.",
    
    # Spring 包名
    "org.springframework.缓存": "org.springframework.cache",
    "org.springframework.数据": "org.springframework.data",
    "org.springframework.安全": "org.springframework.security",
    "org.springframework.网络": "org.springframework.web",
    "org.springframework.启动": "org.springframework.boot",
    "org.springframework.上下文": "org.springframework.context",
    "org.springframework.事务": "org.springframework.transaction",
    "org.springframework.调度": "org.springframework.scheduling",
    "org.springframework.stereotype.组件": "org.springframework.stereotype.Component",
    
    # 其他常见包名
    "jakarta.servlet": "jakarta.servlet",
    "jakarta.持久化": "jakarta.persistence",
    "jakarta.验证": "jakarta.validation",
    "com.fasterxml.jackson": "com.fasterxml.jackson",
    "com.baomidou.mybatisplus": "com.baomidou.mybatisplus",
    "io.jsonwebtoken": "io.jsonwebtoken",
    "lombok": "lombok",
    
    # 注解
    "@服务": "@Service",
    "@控制器": "@Controller",
    "@RestController": "@RestController",
    "@仓库": "@Repository",
    "@组件": "@Component",
    "@配置": "@Configuration",
    "@Bean": "@Bean",
    "@自动装配": "@Autowired",
    "@值": "@Value",
    "@实体": "@Entity",
    "@表": "@Table",
    "@Id": "@Id",
    "@生成值": "@GeneratedValue",
    "@列": "@Column",
    "@多对一": "@ManyToOne",
    "@一对多": "@OneToMany",
    "@多对多": "@ManyToMany",
    "@连接列": "@JoinColumn",
    "@连接表": "@JoinTable",
    "@事务性": "@Transactional",
    "@可缓存": "@Cacheable",
    "@缓存驱逐": "@CacheEvict",
    "@缓存放置": "@CachePut",
    "@异步": "@Async",
    "@计划": "@Scheduled",
    "@启用缓存": "@EnableCaching",
    "@启用异步": "@EnableAsync",
    "@启用调度": "@EnableScheduling",
    "@启用Jpa审计": "@EnableJpaAuditing",
    "@SpringBoot应用": "@SpringBootApplication",
    "@RestControllerAdvice": "@RestControllerAdvice",
    "@异常处理器": "@ExceptionHandler",
    "@请求映射": "@RequestMapping",
    "@获取映射": "@GetMapping",
    "@发布映射": "@PostMapping",
    "@放置映射": "@PutMapping",
    "@删除映射": "@DeleteMapping",
    "@请求体": "@RequestBody",
    "@路径变量": "@PathVariable",
    "@请求参数": "@RequestParam",
    "@请求头": "@RequestHeader",
    "@有效": "@Valid",
    "@不为空": "@NotBlank",
    "@不为Null": "@NotNull",
    "@邮箱": "@Email",
    "@大小": "@Size",
    "@最小": "@Min",
    "@最大": "@Max",
    "@十进制最小": "@DecimalMin",
    "@十进制最大": "@DecimalMax",
    "@模式": "@Pattern",
    "@预授权": "@PreAuthorize",
    "@后授权": "@PostAuthorize",
    "@切面": "@Aspect",
    "@前置": "@Before",
    "@后置": "@After",
    "@环绕": "@Around",
    "@切入点": "@Pointcut",
    "@数据": "@Data",
    "@无参构造": "@NoArgsConstructor",
    "@全参构造": "@AllArgsConstructor",
    "@必需参数构造": "@RequiredArgsConstructor",
    "@Slf4j": "@Slf4j",
    "@构建器": "@Builder",
    "@Getter": "@Getter",
    "@Setter": "@Setter",
    
    # 特殊类名修复
    "AsyncUncaught异常处理器": "AsyncUncaughtExceptionHandler",
    "Enable配置Properties": "EnableConfigurationProperties",
    "配置Properties": "ConfigurationProperties",
    "ConfigurationProperties": "ConfigurationProperties",
    "Configuration": "Configuration",
    "Component": "Component",
    "EnableElastic搜索Repositories": "EnableElasticsearchRepositories",
    "EnableConfigProperties": "EnableConfigurationProperties",
    "ConfigProperties": "ConfigurationProperties",
    "RedisCacheConfig": "RedisCacheConfiguration",
    "PasswordEncoder": "PasswordEncoder",
    "CorsConfig": "CorsConfiguration",
    "CorsConfigSource": "CorsConfigurationSource",
    "UrlBasedCorsConfigSource": "UrlBasedCorsConfigurationSource",
    "UserDetailsService": "UserDetailsService",
    "UserDetails": "UserDetails",
    "CreateTable": "CreateTable",
    
    # 包名修复
    "co.elastic.clients.elastic搜索": "co.elastic.clients.elasticsearch",
    "org.elastic搜索.client": "org.elasticsearch.client",
    "org.springframework.data.elastic搜索": "org.springframework.data.elasticsearch",
    "Elastic搜索Transport": "ElasticsearchTransport",
    "Elastic搜索Client": "ElasticsearchClient",
    "RestClient": "RestClient",
    "Property": "Property",
}

def restore_file(file_path):
    """恢复单个文件"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 应用所有恢复映射
        for chinese, english in RESTORE_MAPPINGS.items():
            content = content.replace(chinese, english)
        
        # 如果内容有变化，写回文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ 已恢复: {file_path}")
            return True
        else:
            print(f"- 无需恢复: {file_path}")
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
    
    print(f"开始恢复 Java 文件...")
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
        print(f"  2. 如果仍有错误，请手动检查和修复")
        print(f"  3. 查看 LOCALIZATION_STATUS.md 了解正确的翻译方法")

if __name__ == '__main__':
    main()
