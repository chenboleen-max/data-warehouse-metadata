package com.kiro.metadata.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Catalog entity
 * Tests entity creation, validation, relationships, and helper methods
 * 
 * Validates: Requirements 5.1, 5.2 (Data Catalog Organization)
 */
class CatalogTest {

    private Catalog catalog;
    private User creator;
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Initialize validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create creator user
        creator = new User();
        creator.setUsername("testuser");
        creator.setEmail("test@example.com");
        creator.setPasswordHash("hashedpassword");
        creator.setRole(UserRole.ADMIN);
        creator.setIsActive(true);

        // Create catalog
        catalog = new Catalog();
        catalog.setName("Business Domain");
        catalog.setDescription("Top level business domain catalog");
        catalog.setLevel(1);
        catalog.setPath("/Business Domain");
        catalog.setCreatedBy(creator);
    }

    @Test
    void testCatalogCreation() {
        assertNotNull(catalog);
        assertEquals("Business Domain", catalog.getName());
        assertEquals("Top level business domain catalog", catalog.getDescription());
        assertEquals(1, catalog.getLevel());
        assertEquals("/Business Domain", catalog.getPath());
        assertEquals(creator, catalog.getCreatedBy());
        assertNull(catalog.getParent());
        assertTrue(catalog.getChildren().isEmpty());
        assertTrue(catalog.getTables().isEmpty());
    }

    @Test
    void testNameValidation() {
        // Valid name
        catalog.setName("Valid Catalog Name");
        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());

        // Blank name should fail validation
        catalog.setName("");
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Catalog name cannot be blank")));

        // Null name should fail validation
        catalog.setName(null);
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNameMaxLength() {
        // Test name within limit (100 characters)
        String validName = "a".repeat(100);
        catalog.setName(validName);
        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());

        // Test name exceeding limit
        String invalidName = "a".repeat(101);
        catalog.setName(invalidName);
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Catalog name must not exceed 100 characters")));
    }

    @Test
    void testDescriptionMaxLength() {
        // Test description within limit (1000 characters)
        String validDescription = "a".repeat(1000);
        catalog.setDescription(validDescription);
        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());

        // Test description exceeding limit
        String invalidDescription = "a".repeat(1001);
        catalog.setDescription(invalidDescription);
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Description must not exceed 1000 characters")));

        // Null description should be allowed
        catalog.setDescription(null);
        violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLevelValidation() {
        // Valid levels (1-5)
        for (int level = 1; level <= 5; level++) {
            catalog.setLevel(level);
            Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
            assertTrue(violations.isEmpty(), "Level " + level + " should be valid");
        }

        // Level 0 should fail validation
        catalog.setLevel(0);
        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Level must be at least 1")));

        // Level 6 should fail validation
        catalog.setLevel(6);
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Level must not exceed 5")));

        // Negative level should fail validation
        catalog.setLevel(-1);
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPathValidation() {
        // Valid path
        catalog.setPath("/root/parent/child");
        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());

        // Blank path should fail validation
        catalog.setPath("");
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Path cannot be blank")));

        // Null path should fail validation
        catalog.setPath(null);
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPathMaxLength() {
        // Test path within limit (500 characters)
        String validPath = "/" + "a".repeat(499);
        catalog.setPath(validPath);
        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());

        // Test path exceeding limit
        String invalidPath = "/" + "a".repeat(500);
        catalog.setPath(invalidPath);
        violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Path must not exceed 500 characters")));
    }

    @Test
    void testSelfReferentialParentRelationship() {
        // Create parent catalog
        Catalog parent = new Catalog();
        parent.setName("Parent");
        parent.setLevel(1);
        parent.setPath("/Parent");
        parent.setCreatedBy(creator);

        // Create child catalog
        Catalog child = new Catalog();
        child.setName("Child");
        child.setLevel(2);
        child.setPath("/Parent/Child");
        child.setCreatedBy(creator);
        child.setParent(parent);

        assertNotNull(child.getParent());
        assertEquals(parent, child.getParent());
        assertEquals("Parent", child.getParent().getName());
    }

    @Test
    void testSelfReferentialChildrenRelationship() {
        // Create child catalogs
        Catalog child1 = new Catalog();
        child1.setName("Child 1");
        child1.setLevel(2);
        child1.setPath("/Business Domain/Child 1");
        child1.setCreatedBy(creator);

        Catalog child2 = new Catalog();
        child2.setName("Child 2");
        child2.setLevel(2);
        child2.setPath("/Business Domain/Child 2");
        child2.setCreatedBy(creator);

        // Add children to parent
        catalog.getChildren().add(child1);
        catalog.getChildren().add(child2);
        child1.setParent(catalog);
        child2.setParent(catalog);

        assertEquals(2, catalog.getChildren().size());
        assertTrue(catalog.getChildren().contains(child1));
        assertTrue(catalog.getChildren().contains(child2));
    }

    @Test
    void testAddChildHelperMethod() {
        // Create child catalog
        Catalog child = new Catalog();
        child.setName("Child");
        child.setLevel(2);
        child.setPath("/Business Domain/Child");
        child.setCreatedBy(creator);

        // Use helper method to add child
        catalog.addChild(child);

        // Verify bidirectional relationship
        assertTrue(catalog.getChildren().contains(child));
        assertEquals(catalog, child.getParent());
        assertEquals(1, catalog.getChildren().size());
    }

    @Test
    void testRemoveChildHelperMethod() {
        // Create and add child catalog
        Catalog child = new Catalog();
        child.setName("Child");
        child.setLevel(2);
        child.setPath("/Business Domain/Child");
        child.setCreatedBy(creator);
        catalog.addChild(child);

        // Verify child was added
        assertEquals(1, catalog.getChildren().size());
        assertEquals(catalog, child.getParent());

        // Use helper method to remove child
        catalog.removeChild(child);

        // Verify bidirectional relationship was cleared
        assertFalse(catalog.getChildren().contains(child));
        assertNull(child.getParent());
        assertEquals(0, catalog.getChildren().size());
    }

    @Test
    void testMultipleLevelHierarchy() {
        // Level 1: Root
        Catalog root = new Catalog();
        root.setName("Root");
        root.setLevel(1);
        root.setPath("/Root");
        root.setCreatedBy(creator);

        // Level 2: Business Domain
        Catalog businessDomain = new Catalog();
        businessDomain.setName("Business Domain");
        businessDomain.setLevel(2);
        businessDomain.setPath("/Root/Business Domain");
        businessDomain.setCreatedBy(creator);
        root.addChild(businessDomain);

        // Level 3: User Domain
        Catalog userDomain = new Catalog();
        userDomain.setName("User Domain");
        userDomain.setLevel(3);
        userDomain.setPath("/Root/Business Domain/User Domain");
        userDomain.setCreatedBy(creator);
        businessDomain.addChild(userDomain);

        // Level 4: User Behavior
        Catalog userBehavior = new Catalog();
        userBehavior.setName("User Behavior");
        userBehavior.setLevel(4);
        userBehavior.setPath("/Root/Business Domain/User Domain/User Behavior");
        userBehavior.setCreatedBy(creator);
        userDomain.addChild(userBehavior);

        // Level 5: Click Events
        Catalog clickEvents = new Catalog();
        clickEvents.setName("Click Events");
        clickEvents.setLevel(5);
        clickEvents.setPath("/Root/Business Domain/User Domain/User Behavior/Click Events");
        clickEvents.setCreatedBy(creator);
        userBehavior.addChild(clickEvents);

        // Verify hierarchy
        assertEquals(1, root.getChildren().size());
        assertEquals(1, businessDomain.getChildren().size());
        assertEquals(1, userDomain.getChildren().size());
        assertEquals(1, userBehavior.getChildren().size());
        assertEquals(0, clickEvents.getChildren().size());

        assertEquals(root, businessDomain.getParent());
        assertEquals(businessDomain, userDomain.getParent());
        assertEquals(userDomain, userBehavior.getParent());
        assertEquals(userBehavior, clickEvents.getParent());
    }

    @Test
    void testManyToManyTableRelationship() {
        // Create table
        TableMetadata table = new TableMetadata();
        table.setDatabaseName("test_db");
        table.setTableName("test_table");
        table.setTableType(TableType.TABLE);
        table.setOwner(creator);

        // Add table to catalog
        catalog.getTables().add(table);
        table.getCatalogs().add(catalog);

        assertEquals(1, catalog.getTables().size());
        assertTrue(catalog.getTables().contains(table));
        assertTrue(table.getCatalogs().contains(catalog));
    }

    @Test
    void testAddTableHelperMethod() {
        // Create table
        TableMetadata table = new TableMetadata();
        table.setDatabaseName("test_db");
        table.setTableName("test_table");
        table.setTableType(TableType.TABLE);
        table.setOwner(creator);

        // Use helper method to add table
        catalog.addTable(table);

        // Verify bidirectional relationship
        assertTrue(catalog.getTables().contains(table));
        assertTrue(table.getCatalogs().contains(catalog));
        assertEquals(1, catalog.getTables().size());
    }

    @Test
    void testRemoveTableHelperMethod() {
        // Create and add table
        TableMetadata table = new TableMetadata();
        table.setDatabaseName("test_db");
        table.setTableName("test_table");
        table.setTableType(TableType.TABLE);
        table.setOwner(creator);
        catalog.addTable(table);

        // Verify table was added
        assertEquals(1, catalog.getTables().size());
        assertTrue(table.getCatalogs().contains(catalog));

        // Use helper method to remove table
        catalog.removeTable(table);

        // Verify bidirectional relationship was cleared
        assertFalse(catalog.getTables().contains(table));
        assertFalse(table.getCatalogs().contains(catalog));
        assertEquals(0, catalog.getTables().size());
    }

    @Test
    void testMultipleTablesInCatalog() {
        // Create multiple tables
        TableMetadata table1 = new TableMetadata();
        table1.setDatabaseName("db1");
        table1.setTableName("table1");
        table1.setTableType(TableType.TABLE);
        table1.setOwner(creator);

        TableMetadata table2 = new TableMetadata();
        table2.setDatabaseName("db2");
        table2.setTableName("table2");
        table2.setTableType(TableType.VIEW);
        table2.setOwner(creator);

        TableMetadata table3 = new TableMetadata();
        table3.setDatabaseName("db3");
        table3.setTableName("table3");
        table3.setTableType(TableType.EXTERNAL);
        table3.setOwner(creator);

        // Add tables to catalog
        catalog.addTable(table1);
        catalog.addTable(table2);
        catalog.addTable(table3);

        assertEquals(3, catalog.getTables().size());
        assertTrue(catalog.getTables().contains(table1));
        assertTrue(catalog.getTables().contains(table2));
        assertTrue(catalog.getTables().contains(table3));
    }

    @Test
    void testTableInMultipleCatalogs() {
        // Create second catalog
        Catalog catalog2 = new Catalog();
        catalog2.setName("Analytics");
        catalog2.setLevel(1);
        catalog2.setPath("/Analytics");
        catalog2.setCreatedBy(creator);

        // Create table
        TableMetadata table = new TableMetadata();
        table.setDatabaseName("shared_db");
        table.setTableName("shared_table");
        table.setTableType(TableType.TABLE);
        table.setOwner(creator);

        // Add table to both catalogs
        catalog.addTable(table);
        catalog2.addTable(table);

        // Verify table is in both catalogs
        assertTrue(catalog.getTables().contains(table));
        assertTrue(catalog2.getTables().contains(table));
        assertEquals(2, table.getCatalogs().size());
        assertTrue(table.getCatalogs().contains(catalog));
        assertTrue(table.getCatalogs().contains(catalog2));
    }

    @Test
    void testCreatedByRelationship() {
        assertNotNull(catalog.getCreatedBy());
        assertEquals(creator, catalog.getCreatedBy());
        assertEquals("testuser", catalog.getCreatedBy().getUsername());
        assertEquals(UserRole.ADMIN, catalog.getCreatedBy().getRole());
    }

    @Test
    void testInheritedBaseEntityFields() {
        // Test that BaseEntity fields are accessible
        assertNotNull(catalog);
        
        // isDeleted should default to false
        assertFalse(catalog.getIsDeleted());
        
        // Test setting isDeleted
        catalog.setIsDeleted(true);
        assertTrue(catalog.getIsDeleted());
    }

    @Test
    void testAllArgsConstructor() {
        // Create child catalog for testing
        Catalog child = new Catalog();
        child.setName("Child");
        child.setLevel(2);
        child.setPath("/Parent/Child");
        child.setCreatedBy(creator);

        // Test constructor with all arguments
        Catalog newCatalog = new Catalog(
            "Test Catalog",
            "Test Description",
            null,
            java.util.List.of(child),
            1,
            "/Test Catalog",
            creator,
            new java.util.HashSet<>()
        );

        assertEquals("Test Catalog", newCatalog.getName());
        assertEquals("Test Description", newCatalog.getDescription());
        assertNull(newCatalog.getParent());
        assertEquals(1, newCatalog.getChildren().size());
        assertEquals(1, newCatalog.getLevel());
        assertEquals("/Test Catalog", newCatalog.getPath());
        assertEquals(creator, newCatalog.getCreatedBy());
        assertTrue(newCatalog.getTables().isEmpty());
    }

    @Test
    void testNoArgsConstructor() {
        // Test no-args constructor
        Catalog newCatalog = new Catalog();
        
        assertNotNull(newCatalog);
        assertNull(newCatalog.getName());
        assertNull(newCatalog.getDescription());
        assertNull(newCatalog.getParent());
        assertNotNull(newCatalog.getChildren());
        assertTrue(newCatalog.getChildren().isEmpty());
        assertNull(newCatalog.getLevel());
        assertNull(newCatalog.getPath());
        assertNull(newCatalog.getCreatedBy());
        assertNotNull(newCatalog.getTables());
        assertTrue(newCatalog.getTables().isEmpty());
    }

    @Test
    void testCompleteValidCatalog() {
        // Test a complete, valid catalog with all fields set
        Catalog validCatalog = new Catalog();
        validCatalog.setName("Complete Catalog");
        validCatalog.setDescription("A complete catalog with all fields set properly");
        validCatalog.setLevel(3);
        validCatalog.setPath("/Root/Parent/Complete Catalog");
        validCatalog.setCreatedBy(creator);

        Set<ConstraintViolation<Catalog>> violations = validator.validate(validCatalog);
        assertTrue(violations.isEmpty(), "A complete valid catalog should have no validation errors");
    }

    @Test
    void testRootCatalog() {
        // Test root level catalog (level 1, no parent)
        Catalog root = new Catalog();
        root.setName("Root");
        root.setDescription("Root level catalog");
        root.setLevel(1);
        root.setPath("/Root");
        root.setCreatedBy(creator);

        Set<ConstraintViolation<Catalog>> violations = validator.validate(root);
        assertTrue(violations.isEmpty());
        assertNull(root.getParent());
        assertEquals(1, root.getLevel());
    }

    @Test
    void testMaxLevelCatalog() {
        // Test maximum level catalog (level 5)
        Catalog maxLevel = new Catalog();
        maxLevel.setName("Max Level");
        maxLevel.setDescription("Maximum level catalog");
        maxLevel.setLevel(5);
        maxLevel.setPath("/L1/L2/L3/L4/Max Level");
        maxLevel.setCreatedBy(creator);

        Set<ConstraintViolation<Catalog>> violations = validator.validate(maxLevel);
        assertTrue(violations.isEmpty());
        assertEquals(5, maxLevel.getLevel());
    }
}
