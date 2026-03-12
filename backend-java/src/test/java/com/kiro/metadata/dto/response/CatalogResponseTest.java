package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CatalogResponse DTO
 */
class CatalogResponseTest {

    @Test
    void testCatalogResponseCreation() {
        UUID id = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CatalogResponse response = CatalogResponse.builder()
                .id(id)
                .name("User Domain")
                .description("Domain for user-related tables")
                .parentId(null)
                .parentName(null)
                .level(1)
                .path("/User Domain")
                .children(new ArrayList<>())
                .tableCount(5)
                .createdBy(createdBy)
                .createdByUsername("admin")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("User Domain");
        assertThat(response.getLevel()).isEqualTo(1);
        assertThat(response.getTableCount()).isEqualTo(5);
        assertThat(response.getChildren()).isEmpty();
    }

    @Test
    void testRootCatalog() {
        CatalogResponse response = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("Business Domain")
                .level(1)
                .path("/Business Domain")
                .parentId(null)
                .parentName(null)
                .tableCount(0)
                .children(new ArrayList<>())
                .build();

        assertThat(response.getParentId()).isNull();
        assertThat(response.getParentName()).isNull();
        assertThat(response.getLevel()).isEqualTo(1);
    }

    @Test
    void testCatalogWithChildren() {
        CatalogResponse child1 = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("User Behavior")
                .level(2)
                .tableCount(3)
                .build();

        CatalogResponse child2 = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("User Profile")
                .level(2)
                .tableCount(2)
                .build();

        CatalogResponse parent = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("User Domain")
                .level(1)
                .children(Arrays.asList(child1, child2))
                .tableCount(5)
                .build();

        assertThat(parent.getChildren()).hasSize(2);
        assertThat(parent.getChildren().get(0).getName()).isEqualTo("User Behavior");
        assertThat(parent.getChildren().get(1).getName()).isEqualTo("User Profile");
    }

    @Test
    void testNestedCatalogPath() {
        UUID parentId = UUID.randomUUID();
        
        CatalogResponse response = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("User Behavior")
                .parentId(parentId)
                .parentName("User Domain")
                .level(2)
                .path("/Business Domain/User Domain/User Behavior")
                .tableCount(3)
                .build();

        assertThat(response.getParentId()).isEqualTo(parentId);
        assertThat(response.getPath()).contains("User Domain");
        assertThat(response.getPath()).contains("User Behavior");
    }

    @Test
    void testMaxLevelCatalog() {
        CatalogResponse response = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("Deep Catalog")
                .level(5)
                .path("/L1/L2/L3/L4/Deep Catalog")
                .tableCount(1)
                .build();

        assertThat(response.getLevel()).isEqualTo(5);
    }

    @Test
    void testEmptyTableCount() {
        CatalogResponse response = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("Empty Catalog")
                .level(1)
                .tableCount(0)
                .build();

        assertThat(response.getTableCount()).isZero();
    }

    @Test
    void testBuilderDefaults() {
        CatalogResponse response = CatalogResponse.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .level(1)
                .build();

        assertThat(response.getChildren()).isNotNull();
        assertThat(response.getChildren()).isEmpty();
    }
}
