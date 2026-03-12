package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ChangeHistoryResponse DTO
 */
class ChangeHistoryResponseTest {

    @Test
    void testChangeHistoryResponseCreation() {
        UUID id = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        UUID changedBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ChangeHistoryResponse response = ChangeHistoryResponse.builder()
                .id(id)
                .entityType("TABLE")
                .entityId(entityId)
                .entityName("users")
                .operation("UPDATE")
                .fieldName("description")
                .oldValue("Old description")
                .newValue("New description")
                .changedAt(now)
                .changedBy(changedBy)
                .changedByUsername("admin")
                .build();

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getEntityType()).isEqualTo("TABLE");
        assertThat(response.getOperation()).isEqualTo("UPDATE");
        assertThat(response.getFieldName()).isEqualTo("description");
    }

    @Test
    void testCreateOperation() {
        ChangeHistoryResponse response = ChangeHistoryResponse.builder()
                .id(UUID.randomUUID())
                .entityType("TABLE")
                .entityId(UUID.randomUUID())
                .operation("CREATE")
                .fieldName(null)
                .oldValue(null)
                .newValue("{\"tableName\":\"users\"}")
                .changedAt(LocalDateTime.now())
                .changedBy(UUID.randomUUID())
                .build();

        assertThat(response.isCreate()).isTrue();
        assertThat(response.isUpdate()).isFalse();
        assertThat(response.isDelete()).isFalse();
        assertThat(response.getOldValue()).isNull();
    }

    @Test
    void testUpdateOperation() {
        ChangeHistoryResponse response = ChangeHistoryResponse.builder()
                .id(UUID.randomUUID())
                .entityType("COLUMN")
                .entityId(UUID.randomUUID())
                .operation("UPDATE")
                .fieldName("dataType")
                .oldValue("VARCHAR(50)")
                .newValue("VARCHAR(100)")
                .changedAt(LocalDateTime.now())
                .changedBy(UUID.randomUUID())
                .build();

        assertThat(response.isCreate()).isFalse();
        assertThat(response.isUpdate()).isTrue();
        assertThat(response.isDelete()).isFalse();
        assertThat(response.getFieldName()).isNotNull();
    }

    @Test
    void testDeleteOperation() {
        ChangeHistoryResponse response = ChangeHistoryResponse.builder()
                .id(UUID.randomUUID())
                .entityType("CATALOG")
                .entityId(UUID.randomUUID())
                .operation("DELETE")
                .fieldName(null)
                .oldValue("{\"name\":\"Old Catalog\"}")
                .newValue(null)
                .changedAt(LocalDateTime.now())
                .changedBy(UUID.randomUUID())
                .build();

        assertThat(response.isCreate()).isFalse();
        assertThat(response.isUpdate()).isFalse();
        assertThat(response.isDelete()).isTrue();
        assertThat(response.getNewValue()).isNull();
    }

    @Test
    void testDifferentEntityTypes() {
        String[] entityTypes = {"TABLE", "COLUMN", "CATALOG"};
        
        for (String entityType : entityTypes) {
            ChangeHistoryResponse response = ChangeHistoryResponse.builder()
                    .id(UUID.randomUUID())
                    .entityType(entityType)
                    .entityId(UUID.randomUUID())
                    .operation("UPDATE")
                    .changedAt(LocalDateTime.now())
                    .changedBy(UUID.randomUUID())
                    .build();

            assertThat(response.getEntityType()).isEqualTo(entityType);
        }
    }

    @Test
    void testJsonValueStorage() {
        String oldJson = "{\"description\":\"Old\",\"type\":\"TABLE\"}";
        String newJson = "{\"description\":\"New\",\"type\":\"VIEW\"}";

        ChangeHistoryResponse response = ChangeHistoryResponse.builder()
                .id(UUID.randomUUID())
                .entityType("TABLE")
                .entityId(UUID.randomUUID())
                .operation("UPDATE")
                .fieldName("metadata")
                .oldValue(oldJson)
                .newValue(newJson)
                .changedAt(LocalDateTime.now())
                .changedBy(UUID.randomUUID())
                .build();

        assertThat(response.getOldValue()).isEqualTo(oldJson);
        assertThat(response.getNewValue()).isEqualTo(newJson);
    }

    @Test
    void testOperationHelperMethods() {
        ChangeHistoryResponse createOp = ChangeHistoryResponse.builder()
                .operation("CREATE")
                .build();
        
        ChangeHistoryResponse updateOp = ChangeHistoryResponse.builder()
                .operation("UPDATE")
                .build();
        
        ChangeHistoryResponse deleteOp = ChangeHistoryResponse.builder()
                .operation("DELETE")
                .build();

        assertThat(createOp.isCreate()).isTrue();
        assertThat(updateOp.isUpdate()).isTrue();
        assertThat(deleteOp.isDelete()).isTrue();
    }

    @Test
    void testInvalidOperationHelperMethods() {
        ChangeHistoryResponse response = ChangeHistoryResponse.builder()
                .operation("INVALID")
                .build();

        assertThat(response.isCreate()).isFalse();
        assertThat(response.isUpdate()).isFalse();
        assertThat(response.isDelete()).isFalse();
    }
}
