package com.kiro.metadata.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SwaggerConfig 配置类测试
 * 验证 Swagger/OpenAPI 3 配置是否正确
 * 
 * @author Kiro
 * @since 1.0.0
 */
@SpringBootTest
@DisplayName("Swagger 配置测试")
class SwaggerConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    @DisplayName("应该正确配置 API 基本信息")
    void shouldConfigureApiInfo() {
        // Given & When
        Info info = openAPI.getInfo();

        // Then
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("数据仓库元数据管理系统 API");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
        assertThat(info.getDescription()).isNotBlank();
        assertThat(info.getDescription()).contains("数据仓库元数据管理系统");
        assertThat(info.getDescription()).contains("JWT");
        assertThat(info.getDescription()).contains("认证说明");
    }

    @Test
    @DisplayName("应该正确配置联系信息")
    void shouldConfigureContactInfo() {
        // Given & When
        var contact = openAPI.getInfo().getContact();

        // Then
        assertThat(contact).isNotNull();
        assertThat(contact.getName()).isEqualTo("Kiro Development Team");
        assertThat(contact.getEmail()).isEqualTo("dev@kiro.com");
        assertThat(contact.getUrl()).isEqualTo("https://github.com/kiro/metadata-management");
    }

    @Test
    @DisplayName("应该正确配置许可证信息")
    void shouldConfigureLicenseInfo() {
        // Given & When
        var license = openAPI.getInfo().getLicense();

        // Then
        assertThat(license).isNotNull();
        assertThat(license.getName()).isEqualTo("Apache 2.0");
        assertThat(license.getUrl()).isEqualTo("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    @Test
    @DisplayName("应该正确配置服务器列表")
    void shouldConfigureServers() {
        // Given & When
        List<Server> servers = openAPI.getServers();

        // Then
        assertThat(servers).isNotNull();
        assertThat(servers).hasSize(3);
        
        // 验证本地开发环境
        Server localServer = servers.get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8080/api");
        assertThat(localServer.getDescription()).isEqualTo("本地开发环境");
        
        // 验证开发环境
        Server devServer = servers.get(1);
        assertThat(devServer.getUrl()).isEqualTo("https://api-dev.kiro.com");
        assertThat(devServer.getDescription()).isEqualTo("开发环境");
        
        // 验证生产环境
        Server prodServer = servers.get(2);
        assertThat(prodServer.getUrl()).isEqualTo("https://api.kiro.com");
        assertThat(prodServer.getDescription()).isEqualTo("生产环境");
    }

    @Test
    @DisplayName("应该正确配置 JWT 安全认证方案")
    void shouldConfigureJwtSecurityScheme() {
        // Given & When
        var components = openAPI.getComponents();
        var securitySchemes = components.getSecuritySchemes();

        // Then
        assertThat(securitySchemes).isNotNull();
        assertThat(securitySchemes).containsKey("Bearer Authentication");
        
        SecurityScheme jwtScheme = securitySchemes.get("Bearer Authentication");
        assertThat(jwtScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(jwtScheme.getScheme()).isEqualTo("bearer");
        assertThat(jwtScheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(jwtScheme.getDescription()).isNotBlank();
        assertThat(jwtScheme.getDescription()).contains("JWT 认证令牌");
        assertThat(jwtScheme.getDescription()).contains("Authorization: Bearer");
    }

    @Test
    @DisplayName("应该配置全局安全要求")
    void shouldConfigureGlobalSecurityRequirement() {
        // Given & When
        List<SecurityRequirement> securityRequirements = openAPI.getSecurity();

        // Then
        assertThat(securityRequirements).isNotNull();
        assertThat(securityRequirements).isNotEmpty();
        
        SecurityRequirement requirement = securityRequirements.get(0);
        assertThat(requirement.containsKey("Bearer Authentication")).isTrue();
    }

    @Test
    @DisplayName("应该正确配置 API 标签分组")
    void shouldConfigureApiTags() {
        // Given & When
        List<Tag> tags = openAPI.getTags();

        // Then
        assertThat(tags).isNotNull();
        assertThat(tags).hasSize(9);
        
        // 验证标签名称
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .toList();
        
        assertThat(tagNames).containsExactly(
                "认证",
                "表元数据",
                "字段元数据",
                "血缘关系",
                "搜索",
                "数据目录",
                "数据质量",
                "变更历史",
                "导入导出"
        );
        
        // 验证每个标签都有描述
        tags.forEach(tag -> {
            assertThat(tag.getDescription()).isNotBlank();
        });
    }

    @Test
    @DisplayName("应该为认证标签配置正确的描述")
    void shouldConfigureAuthenticationTagDescription() {
        // Given & When
        Tag authTag = openAPI.getTags().stream()
                .filter(tag -> "认证".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(authTag.getDescription()).contains("用户认证");
        assertThat(authTag.getDescription()).contains("登录");
        assertThat(authTag.getDescription()).contains("令牌");
    }

    @Test
    @DisplayName("应该为表元数据标签配置正确的描述")
    void shouldConfigureTableMetadataTagDescription() {
        // Given & When
        Tag tableTag = openAPI.getTags().stream()
                .filter(tag -> "表元数据".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(tableTag.getDescription()).contains("数据表");
        assertThat(tableTag.getDescription()).contains("元数据管理");
        assertThat(tableTag.getDescription()).contains("创建");
        assertThat(tableTag.getDescription()).contains("查询");
    }

    @Test
    @DisplayName("应该为血缘关系标签配置正确的描述")
    void shouldConfigureLineageTagDescription() {
        // Given & When
        Tag lineageTag = openAPI.getTags().stream()
                .filter(tag -> "血缘关系".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(lineageTag.getDescription()).contains("血缘关系");
        assertThat(lineageTag.getDescription()).contains("图谱");
        assertThat(lineageTag.getDescription()).contains("影响分析");
    }

    @Test
    @DisplayName("应该为搜索标签配置正确的描述")
    void shouldConfigureSearchTagDescription() {
        // Given & When
        Tag searchTag = openAPI.getTags().stream()
                .filter(tag -> "搜索".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(searchTag.getDescription()).contains("全文搜索");
        assertThat(searchTag.getDescription()).contains("表名");
        assertThat(searchTag.getDescription()).contains("字段名");
    }

    @Test
    @DisplayName("应该为数据目录标签配置正确的描述")
    void shouldConfigureCatalogTagDescription() {
        // Given & When
        Tag catalogTag = openAPI.getTags().stream()
                .filter(tag -> "数据目录".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(catalogTag.getDescription()).contains("数据目录");
        assertThat(catalogTag.getDescription()).contains("多级目录");
        assertThat(catalogTag.getDescription()).contains("分类");
    }

    @Test
    @DisplayName("应该为数据质量标签配置正确的描述")
    void shouldConfigureQualityTagDescription() {
        // Given & When
        Tag qualityTag = openAPI.getTags().stream()
                .filter(tag -> "数据质量".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(qualityTag.getDescription()).contains("数据质量");
        assertThat(qualityTag.getDescription()).contains("指标");
        assertThat(qualityTag.getDescription()).contains("趋势");
    }

    @Test
    @DisplayName("应该为变更历史标签配置正确的描述")
    void shouldConfigureHistoryTagDescription() {
        // Given & When
        Tag historyTag = openAPI.getTags().stream()
                .filter(tag -> "变更历史".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(historyTag.getDescription()).contains("变更历史");
        assertThat(historyTag.getDescription()).contains("追踪");
        assertThat(historyTag.getDescription()).contains("版本对比");
    }

    @Test
    @DisplayName("应该为导入导出标签配置正确的描述")
    void shouldConfigureImportExportTagDescription() {
        // Given & When
        Tag importExportTag = openAPI.getTags().stream()
                .filter(tag -> "导入导出".equals(tag.getName()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(importExportTag.getDescription()).contains("导入导出");
        assertThat(importExportTag.getDescription()).contains("CSV");
        assertThat(importExportTag.getDescription()).contains("JSON");
        assertThat(importExportTag.getDescription()).contains("异步");
    }
}
