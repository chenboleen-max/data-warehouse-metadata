package com.kiro.metadata.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChangeHistory entity
 * Tests entity creation, validation, relationships, and constraints
 * 
 * Validates: Requirements 8.1, 8.2 (Metadata Change History)
 */
class ChangeHistoryTest {

    private ChangeHistory changeHistory;
    private User user;
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Initialize validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create user
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(UserRole.DEVELOPER);
        user.setIsActive(true);

        // Create change history
        changeHistory = new ChangeHistory();
        changeHistory.setEntityType("TableMetadata");
        changeHistory.setEntityId(UUID.randomUUID());
        changeHistory.setOperation(OperationType.UPDATE);
        changeHistory.setFieldName("description");
        changeHistory.setOldValue("{\"description\":\"Old description\"}");
        changeHistory.setNewValue("{\"description\":\"New description\"}");
        changeHistory.setChangedAt(LocalDateTime.now());
        changeHistory.setChangedBy(user);
    }

    @Test
    void testChangeHistoryCreation() {
        assertNotNull(changeHistory);
        assertEquals("TableMetadata", changeHistory.getEntityType());
        assertNotNull(changeHistory.getEntityId());
        assertEquals(OperationType.UPDATE, changeHistory.getOperation());
        assertEquals("description", changeHistory.getFieldName());
        assertEquals("{\"description\":\"Old description\"}", changeHistory.getOldValue());
        assertEquals("{\"description\":\"New description\"}", changeHistory.getNewValue());
        assertNotNull(changeHistory.getChangedAt());
        assertEquals(user, changeHistory.getChangedBy());
    }

    @Test
    void testEntityTypeValidation() {
        // Valid entity type
        changeHistory.setEntityType("TableMetadata");
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Blank entity type should fail validation
        changeHistory.setEntityType("");
        violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Entity type cannot be blank")));

        // Null entity type should fail validation
        changeHistory.setEntityType(null);
        violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Entity type cannot be blank")));
    }

    @Test
    void testEntityTypeLengthValidation() {
        // Valid length (50 characters)
        changeHistory.setEntityType("A".repeat(50));
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Exceeds maximum length (51 characters)
        changeHistory.setEntityType("A".repeat(51));
        violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Entity type must not exceed 50 characters")));
    }

    @Test
    void testEntityIdValidation() {
        // Valid entity ID
        changeHistory.setEntityId(UUID.randomUUID());
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Null entity ID should fail validation
        changeHistory.setEntityId(null);
        violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Entity ID cannot be null")));
    }

    @Test
    void testOperationTypeValidation() {
        // Test all operation types
        for (OperationType operation : OperationType.values()) {
            changeHistory.setOperation(operation);
            Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
            assertTrue(violations.isEmpty(), "Operation type " + operation + " should be valid");
        }

        // Null operation should fail validation
        changeHistory.setOperation(null);
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Operation type cannot be null")));
    }

    @Test
    void testFieldNameOptional() {
        // Field name can be null (optional for CREATE and DELETE operations)
        changeHistory.setFieldName(null);
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Valid field name
        changeHistory.setFieldName("tableName");
        violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFieldNameLengthValidation() {
        // Valid length (100 characters)
        changeHistory.setFieldName("A".repeat(100));
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Exceeds maximum length (101 characters)
        changeHistory.setFieldName("A".repeat(101));
        violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Field name must not exceed 100 characters")));
    }

    @Test
    void testOldValueOptional() {
        // Old value can be null (for CREATE operations)
        changeHistory.setOldValue(null);
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Valid JSON string
        changeHistory.setOldValue("{\"key\":\"value\"}");
        violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNewValueOptional() {
        // New value can be null (for DELETE operations)
        changeHistory.setNewValue(null);
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Valid JSON string
        changeHistory.setNewValue("{\"key\":\"value\"}");
        violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testChangedAtValidation() {
        // Valid changed at timestamp
        changeHistory.setChangedAt(LocalDateTime.now());
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Null changed at should fail validation
        changeHistory.setChangedAt(null);
        violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Changed at timestamp cannot be null")));
    }

    @Test
    void testChangedByValidation() {
        // Valid changed by user
        changeHistory.setChangedBy(user);
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Null changed by should fail validation
        changeHistory.setChangedBy(null);
        violations = validator.validate(changeHistory);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Changed by user cannot be null")));
    }

    @Test
    void testCreateOperation() {
        // CREATE operation should have no old value
        ChangeHistory createHistory = new ChangeHistory();
        createHistory.setEntityType("TableMetadata");
        createHistory.setEntityId(UUID.randomUUID());
        createHistory.setOperation(OperationType.CREATE);
        createHistory.setFieldName(null);
        createHistory.setOldValue(null);
        createHistory.setNewValue("{\"tableName\":\"new_table\",\"databaseName\":\"test_db\"}");
        createHistory.setChangedAt(LocalDateTime.now());
        createHistory.setChangedBy(user);

        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(createHistory);
        assertTrue(violations.isEmpty());
        assertNull(createHistory.getOldValue());
        assertNull(createHistory.getFieldName());
        assertNotNull(createHistory.getNewValue());
    }

    @Test
    void testUpdateOperation() {
        // UPDATE operation should have both old and new values
        ChangeHistory updateHistory = new ChangeHistory();
        updateHistory.setEntityType("TableMetadata");
        updateHistory.setEntityId(UUID.randomUUID());
        updateHistory.setOperation(OperationType.UPDATE);
        updateHistory.setFieldName("description");
        updateHistory.setOldValue("{\"description\":\"Old\"}");
        updateHistory.setNewValue("{\"description\":\"New\"}");
        updateHistory.setChangedAt(LocalDateTime.now());
        updateHistory.setChangedBy(user);

        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(updateHistory);
        assertTrue(violations.isEmpty());
        assertNotNull(updateHistory.getOldValue());
        assertNotNull(updateHistory.getNewValue());
        assertNotNull(updateHistory.getFieldName());
    }

    @Test
    void testDeleteOperation() {
        // DELETE operation should have no new value
        ChangeHistory deleteHistory = new ChangeHistory();
        deleteHistory.setEntityType("TableMetadata");
        deleteHistory.setEntityId(UUID.randomUUID());
        deleteHistory.setOperation(OperationType.DELETE);
        deleteHistory.setFieldName(null);
        deleteHistory.setOldValue("{\"tableName\":\"deleted_table\"}");
        deleteHistory.setNewValue(null);
        deleteHistory.setChangedAt(LocalDateTime.now());
        deleteHistory.setChangedBy(user);

        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(deleteHistory);
        assertTrue(violations.isEmpty());
        assertNotNull(deleteHistory.getOldValue());
        assertNull(deleteHistory.getNewValue());
        assertNull(deleteHistory.getFieldName());
    }

    @Test
    void testManyToOneUserRelationship() {
        assertNotNull(changeHistory.getChangedBy());
        assertEquals(user, changeHistory.getChangedBy());
        assertEquals("testuser", changeHistory.getChangedBy().getUsername());
        assertEquals("test@example.com", changeHistory.getChangedBy().getEmail());
        assertEquals(UserRole.DEVELOPER, changeHistory.getChangedBy().getRole());
    }

    @Test
    void testMultipleChangesForSameEntity() {
        UUID entityId = UUID.randomUUID();

        // First change
        ChangeHistory change1 = new ChangeHistory();
        change1.setEntityType("TableMetadata");
        change1.setEntityId(entityId);
        change1.setOperation(OperationType.UPDATE);
        change1.setFieldName("description");
        change1.setOldValue("{\"description\":\"Original\"}");
        change1.setNewValue("{\"description\":\"First Update\"}");
        change1.setChangedAt(LocalDateTime.now().minusHours(2));
        change1.setChangedBy(user);

        // Second change
        ChangeHistory change2 = new ChangeHistory();
        change2.setEntityType("TableMetadata");
        change2.setEntityId(entityId);
        change2.setOperation(OperationType.UPDATE);
        change2.setFieldName("description");
        change2.setOldValue("{\"description\":\"First Update\"}");
        change2.setNewValue("{\"description\":\"Second Update\"}");
        change2.setChangedAt(LocalDateTime.now());
        change2.setChangedBy(user);

        // Both changes should be valid and reference the same entity
        assertEquals(entityId, change1.getEntityId());
        assertEquals(entityId, change2.getEntityId());
        assertTrue(change2.getChangedAt().isAfter(change1.getChangedAt()));
    }

    @Test
    void testInheritedBaseEntityFields() {
        // Test that BaseEntity fields are accessible
        assertNotNull(changeHistory);
        
        // isDeleted should default to false
        assertFalse(changeHistory.getIsDeleted());
        
        // Test setting isDeleted
        changeHistory.setIsDeleted(true);
        assertTrue(changeHistory.getIsDeleted());
    }

    @Test
    void testAllArgsConstructor() {
        // Test constructor with all arguments
        UUID entityId = UUID.randomUUID();
        LocalDateTime changedTime = LocalDateTime.now();
        
        ChangeHistory newHistory = new ChangeHistory(
            "Column",
            entityId,
            OperationType.CREATE,
            null,
            null,
            "{\"columnName\":\"new_column\"}",
            changedTime,
            user
        );

        assertEquals("Column", newHistory.getEntityType());
        assertEquals(entityId, newHistory.getEntityId());
        assertEquals(OperationType.CREATE, newHistory.getOperation());
        assertNull(newHistory.getFieldName());
        assertNull(newHistory.getOldValue());
        assertEquals("{\"columnName\":\"new_column\"}", newHistory.getNewValue());
        assertEquals(changedTime, newHistory.getChangedAt());
        assertEquals(user, newHistory.getChangedBy());
    }

    @Test
    void testNoArgsConstructor() {
        // Test no-args constructor
        ChangeHistory newHistory = new ChangeHistory();
        
        assertNotNull(newHistory);
        assertNull(newHistory.getEntityType());
        assertNull(newHistory.getEntityId());
        assertNull(newHistory.getOperation());
        assertNull(newHistory.getFieldName());
        assertNull(newHistory.getOldValue());
        assertNull(newHistory.getNewValue());
        assertNull(newHistory.getChangedAt());
        assertNull(newHistory.getChangedBy());
    }

    @Test
    void testCompleteValidChangeHistory() {
        // Test a complete, valid change history with all fields set
        ChangeHistory validHistory = new ChangeHistory();
        validHistory.setEntityType("Lineage");
        validHistory.setEntityId(UUID.randomUUID());
        validHistory.setOperation(OperationType.UPDATE);
        validHistory.setFieldName("transformationLogic");
        validHistory.setOldValue("{\"logic\":\"SELECT * FROM source\"}");
        validHistory.setNewValue("{\"logic\":\"SELECT id, name FROM source WHERE active = true\"}");
        validHistory.setChangedAt(LocalDateTime.now());
        validHistory.setChangedBy(user);

        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(validHistory);
        assertTrue(violations.isEmpty(), "A complete valid change history should have no validation errors");
    }

    @Test
    void testMinimalValidChangeHistory() {
        // Test minimal valid change history (only required fields)
        ChangeHistory minimalHistory = new ChangeHistory();
        minimalHistory.setEntityType("Catalog");
        minimalHistory.setEntityId(UUID.randomUUID());
        minimalHistory.setOperation(OperationType.DELETE);
        minimalHistory.setChangedAt(LocalDateTime.now());
        minimalHistory.setChangedBy(user);

        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(minimalHistory);
        assertTrue(violations.isEmpty(), "Minimal change history with only required fields should be valid");
        
        assertNull(minimalHistory.getFieldName());
        assertNull(minimalHistory.getOldValue());
        assertNull(minimalHistory.getNewValue());
    }

    @Test
    void testJsonValueStorage() {
        // Test storing complex JSON strings
        String complexOldValue = "{\"tableName\":\"users\",\"columns\":[\"id\",\"name\",\"email\"],\"rowCount\":1000}";
        String complexNewValue = "{\"tableName\":\"users\",\"columns\":[\"id\",\"name\",\"email\",\"phone\"],\"rowCount\":1050}";
        
        changeHistory.setOldValue(complexOldValue);
        changeHistory.setNewValue(complexNewValue);

        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());
        assertEquals(complexOldValue, changeHistory.getOldValue());
        assertEquals(complexNewValue, changeHistory.getNewValue());
    }

    @Test
    void testDifferentEntityTypes() {
        // Test tracking changes for different entity types
        String[] entityTypes = {"TableMetadata", "Column", "Lineage", "Catalog", "QualityMetrics"};
        
        for (String entityType : entityTypes) {
            changeHistory.setEntityType(entityType);
            Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
            assertTrue(violations.isEmpty(), "Entity type '" + entityType + "' should be valid");
        }
    }

    @Test
    void testChangedAtTimestamps() {
        // Test various timestamp scenarios
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime lastWeek = now.minusWeeks(1);

        // Current change
        changeHistory.setChangedAt(now);
        Set<ConstraintViolation<ChangeHistory>> violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Historical change (yesterday)
        changeHistory.setChangedAt(yesterday);
        violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());

        // Historical change (last week)
        changeHistory.setChangedAt(lastWeek);
        violations = validator.validate(changeHistory);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleFieldUpdates() {
        // Test tracking multiple field updates for the same entity
        UUID entityId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.now();

        // Update field 1
        ChangeHistory change1 = new ChangeHistory();
        change1.setEntityType("TableMetadata");
        change1.setEntityId(entityId);
        change1.setOperation(OperationType.UPDATE);
        change1.setFieldName("description");
        change1.setOldValue("{\"description\":\"Old\"}");
        change1.setNewValue("{\"description\":\"New\"}");
        change1.setChangedAt(baseTime);
        change1.setChangedBy(user);

        // Update field 2
        ChangeHistory change2 = new ChangeHistory();
        change2.setEntityType("TableMetadata");
        change2.setEntityId(entityId);
        change2.setOperation(OperationType.UPDATE);
        change2.setFieldName("storageFormat");
        change2.setOldValue("{\"storageFormat\":\"PARQUET\"}");
        change2.setNewValue("{\"storageFormat\":\"ORC\"}");
        change2.setChangedAt(baseTime.plusSeconds(1));
        change2.setChangedBy(user);

        Set<ConstraintViolation<ChangeHistory>> violations1 = validator.validate(change1);
        Set<ConstraintViolation<ChangeHistory>> violations2 = validator.validate(change2);
        
        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertEquals(entityId, change1.getEntityId());
        assertEquals(entityId, change2.getEntityId());
        assertNotEquals(change1.getFieldName(), change2.getFieldName());
    }

    @Test
    void testDifferentUsersChanges() {
        // Test changes made by different users
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("hashedpassword");
        admin.setRole(UserRole.ADMIN);
        admin.setIsActive(true);

        UUID entityId = UUID.randomUUID();

        // Change by developer
        ChangeHistory devChange = new ChangeHistory();
        devChange.setEntityType("TableMetadata");
        devChange.setEntityId(entityId);
        devChange.setOperation(OperationType.UPDATE);
        devChange.setFieldName("description");
        devChange.setOldValue("{\"description\":\"Old\"}");
        devChange.setNewValue("{\"description\":\"Updated by dev\"}");
        devChange.setChangedAt(LocalDateTime.now().minusHours(1));
        devChange.setChangedBy(user);

        // Change by admin
        ChangeHistory adminChange = new ChangeHistory();
        adminChange.setEntityType("TableMetadata");
        adminChange.setEntityId(entityId);
        adminChange.setOperation(OperationType.UPDATE);
        adminChange.setFieldName("description");
        adminChange.setOldValue("{\"description\":\"Updated by dev\"}");
        adminChange.setNewValue("{\"description\":\"Updated by admin\"}");
        adminChange.setChangedAt(LocalDateTime.now());
        adminChange.setChangedBy(admin);

        Set<ConstraintViolation<ChangeHistory>> violations1 = validator.validate(devChange);
        Set<ConstraintViolation<ChangeHistory>> violations2 = validator.validate(adminChange);
        
        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertEquals(UserRole.DEVELOPER, devChange.getChangedBy().getRole());
        assertEquals(UserRole.ADMIN, adminChange.getChangedBy().getRole());
    }

    @Test
    void testEntityLifecycle() {
        // Test complete entity lifecycle: CREATE -> UPDATE -> DELETE
        UUID entityId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.now();

        // CREATE
        ChangeHistory createChange = new ChangeHistory();
        createChange.setEntityType("TableMetadata");
        createChange.setEntityId(entityId);
        createChange.setOperation(OperationType.CREATE);
        createChange.setOldValue(null);
        createChange.setNewValue("{\"tableName\":\"test_table\"}");
        createChange.setChangedAt(baseTime);
        createChange.setChangedBy(user);

        // UPDATE
        ChangeHistory updateChange = new ChangeHistory();
        updateChange.setEntityType("TableMetadata");
        updateChange.setEntityId(entityId);
        updateChange.setOperation(OperationType.UPDATE);
        updateChange.setFieldName("description");
        updateChange.setOldValue("{\"description\":null}");
        updateChange.setNewValue("{\"description\":\"Added description\"}");
        updateChange.setChangedAt(baseTime.plusHours(1));
        updateChange.setChangedBy(user);

        // DELETE
        ChangeHistory deleteChange = new ChangeHistory();
        deleteChange.setEntityType("TableMetadata");
        deleteChange.setEntityId(entityId);
        deleteChange.setOperation(OperationType.DELETE);
        deleteChange.setOldValue("{\"tableName\":\"test_table\"}");
        deleteChange.setNewValue(null);
        deleteChange.setChangedAt(baseTime.plusHours(2));
        deleteChange.setChangedBy(user);

        Set<ConstraintViolation<ChangeHistory>> violations1 = validator.validate(createChange);
        Set<ConstraintViolation<ChangeHistory>> violations2 = validator.validate(updateChange);
        Set<ConstraintViolation<ChangeHistory>> violations3 = validator.validate(deleteChange);
        
        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertTrue(violations3.isEmpty());
        
        assertEquals(OperationType.CREATE, createChange.getOperation());
        assertEquals(OperationType.UPDATE, updateChange.getOperation());
        assertEquals(OperationType.DELETE, deleteChange.getOperation());
        
        assertTrue(updateChange.getChangedAt().isAfter(createChange.getChangedAt()));
        assertTrue(deleteChange.getChangedAt().isAfter(updateChange.getChangedAt()));
    }
}
