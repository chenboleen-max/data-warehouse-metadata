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
 * Unit tests for ColumnMetadata entity
 * Tests entity creation, validation, and relationships
 * 
 * Validates: Requirements 2.1 (Column Metadata Management)
 */
class ColumnMetadataTest {

    private ColumnMetadata column;
    private TableMetadata table;
    private User owner;
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Initialize validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create owner user
        owner = new User();
        owner.setUsername("testuser");
        owner.setEmail("test@example.com");
        owner.setPasswordHash("hashedpassword");
        owner.setRole(UserRole.DEVELOPER);
        owner.setIsActive(true);

        // Create table metadata
        table = new TableMetadata();
        table.setDatabaseName("test_db");
        table.setTableName("test_table");
        table.setTableType(TableType.TABLE);
        table.setOwner(owner);

        // Create column metadata
        column = new ColumnMetadata();
        column.setTable(table);
        column.setColumnName("user_id");
        column.setDataType("BIGINT");
        column.setColumnOrder(1);
        column.setIsNullable(false);
        column.setIsPartitionKey(false);
        column.setDescription("User identifier");
    }

    @Test
    void testColumnMetadataCreation() {
        assertNotNull(column);
        assertEquals("user_id", column.getColumnName());
        assertEquals("BIGINT", column.getDataType());
        assertEquals(1, column.getColumnOrder());
        assertFalse(column.getIsNullable());
        assertFalse(column.getIsPartitionKey());
        assertEquals("User identifier", column.getDescription());
        assertEquals(table, column.getTable());
    }

    @Test
    void testDefaultValues() {
        ColumnMetadata newColumn = new ColumnMetadata();
        
        // isNullable should default to true
        assertTrue(newColumn.getIsNullable());
        
        // isPartitionKey should default to false
        assertFalse(newColumn.getIsPartitionKey());
    }

    @Test
    void testTableRelationship() {
        assertNotNull(column.getTable());
        assertEquals(table, column.getTable());
        assertEquals("test_db", column.getTable().getDatabaseName());
        assertEquals("test_table", column.getTable().getTableName());
    }

    @Test
    void testColumnNameValidation() {
        // Valid column name
        column.setColumnName("valid_column_name");
        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
        assertTrue(violations.isEmpty());

        // Blank column name should fail validation
        column.setColumnName("");
        violations = validator.validate(column);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Column name cannot be blank")));

        // Null column name should fail validation
        column.setColumnName(null);
        violations = validator.validate(column);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testDataTypeValidation() {
        // Valid data types
        String[] validTypes = {"VARCHAR", "INT", "BIGINT", "DECIMAL", "TIMESTAMP", "BOOLEAN"};
        for (String type : validTypes) {
            column.setDataType(type);
            Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
            assertTrue(violations.isEmpty(), "Data type " + type + " should be valid");
        }

        // Blank data type should fail validation
        column.setDataType("");
        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Data type cannot be blank")));

        // Null data type should fail validation
        column.setDataType(null);
        violations = validator.validate(column);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testColumnOrderValidation() {
        // Valid column orders
        column.setColumnOrder(1);
        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
        assertTrue(violations.isEmpty());

        column.setColumnOrder(100);
        violations = validator.validate(column);
        assertTrue(violations.isEmpty());

        // Null column order should fail validation
        column.setColumnOrder(null);
        violations = validator.validate(column);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Column order cannot be null")));
    }

    @Test
    void testIsNullableFlag() {
        // Test nullable column
        column.setIsNullable(true);
        assertTrue(column.getIsNullable());

        // Test non-nullable column
        column.setIsNullable(false);
        assertFalse(column.getIsNullable());
    }

    @Test
    void testIsPartitionKeyFlag() {
        // Test non-partition key column
        column.setIsPartitionKey(false);
        assertFalse(column.getIsPartitionKey());

        // Test partition key column
        column.setIsPartitionKey(true);
        assertTrue(column.getIsPartitionKey());
    }

    @Test
    void testDescriptionField() {
        // Test with description
        column.setDescription("This is a test column");
        assertEquals("This is a test column", column.getDescription());

        // Test with null description (should be allowed)
        column.setDescription(null);
        assertNull(column.getDescription());
        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
        assertTrue(violations.isEmpty());

        // Test with empty description (should be allowed)
        column.setDescription("");
        assertEquals("", column.getDescription());
    }

    @Test
    void testDescriptionMaxLength() {
        // Test description within limit (1000 characters)
        String validDescription = "a".repeat(1000);
        column.setDescription(validDescription);
        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
        assertTrue(violations.isEmpty());

        // Test description exceeding limit
        String invalidDescription = "a".repeat(1001);
        column.setDescription(invalidDescription);
        violations = validator.validate(column);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Description must not exceed 1000 characters")));
    }

    @Test
    void testColumnNameMaxLength() {
        // Test column name within limit (100 characters)
        String validName = "a".repeat(100);
        column.setColumnName(validName);
        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
        assertTrue(violations.isEmpty());

        // Test column name exceeding limit
        String invalidName = "a".repeat(101);
        column.setColumnName(invalidName);
        violations = validator.validate(column);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Column name must not exceed 100 characters")));
    }

    @Test
    void testDataTypeMaxLength() {
        // Test data type within limit (50 characters)
        String validType = "a".repeat(50);
        column.setDataType(validType);
        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(column);
        assertTrue(violations.isEmpty());

        // Test data type exceeding limit
        String invalidType = "a".repeat(51);
        column.setDataType(invalidType);
        violations = validator.validate(column);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Data type must not exceed 50 characters")));
    }

    @Test
    void testMultipleColumnsInTable() {
        // Create multiple columns for the same table
        ColumnMetadata column1 = new ColumnMetadata();
        column1.setTable(table);
        column1.setColumnName("id");
        column1.setDataType("BIGINT");
        column1.setColumnOrder(1);

        ColumnMetadata column2 = new ColumnMetadata();
        column2.setTable(table);
        column2.setColumnName("name");
        column2.setDataType("VARCHAR");
        column2.setColumnOrder(2);

        ColumnMetadata column3 = new ColumnMetadata();
        column3.setTable(table);
        column3.setColumnName("created_at");
        column3.setDataType("TIMESTAMP");
        column3.setColumnOrder(3);

        // Add columns to table
        table.addColumn(column1);
        table.addColumn(column2);
        table.addColumn(column3);

        assertEquals(3, table.getColumns().size());
        assertTrue(table.getColumns().contains(column1));
        assertTrue(table.getColumns().contains(column2));
        assertTrue(table.getColumns().contains(column3));
    }

    @Test
    void testPartitionKeyColumn() {
        // Create a partition key column
        ColumnMetadata partitionColumn = new ColumnMetadata();
        partitionColumn.setTable(table);
        partitionColumn.setColumnName("date_partition");
        partitionColumn.setDataType("DATE");
        partitionColumn.setColumnOrder(10);
        partitionColumn.setIsNullable(false);
        partitionColumn.setIsPartitionKey(true);
        partitionColumn.setDescription("Date partition key");

        assertTrue(partitionColumn.getIsPartitionKey());
        assertFalse(partitionColumn.getIsNullable());
        assertEquals("date_partition", partitionColumn.getColumnName());
    }

    @Test
    void testInheritedBaseEntityFields() {
        // Test that BaseEntity fields are accessible
        assertNotNull(column);
        
        // isDeleted should default to false
        assertFalse(column.getIsDeleted());
        
        // Test setting isDeleted
        column.setIsDeleted(true);
        assertTrue(column.getIsDeleted());
    }

    @Test
    void testAllArgsConstructor() {
        // Test constructor with all arguments
        ColumnMetadata newColumn = new ColumnMetadata(
            table,
            "test_column",
            "VARCHAR",
            5,
            true,
            false,
            "Test description"
        );

        assertEquals(table, newColumn.getTable());
        assertEquals("test_column", newColumn.getColumnName());
        assertEquals("VARCHAR", newColumn.getDataType());
        assertEquals(5, newColumn.getColumnOrder());
        assertTrue(newColumn.getIsNullable());
        assertFalse(newColumn.getIsPartitionKey());
        assertEquals("Test description", newColumn.getDescription());
    }

    @Test
    void testNoArgsConstructor() {
        // Test no-args constructor
        ColumnMetadata newColumn = new ColumnMetadata();
        
        assertNotNull(newColumn);
        assertNull(newColumn.getTable());
        assertNull(newColumn.getColumnName());
        assertNull(newColumn.getDataType());
        assertNull(newColumn.getColumnOrder());
        assertTrue(newColumn.getIsNullable()); // default value
        assertFalse(newColumn.getIsPartitionKey()); // default value
        assertNull(newColumn.getDescription());
    }

    @Test
    void testCompleteValidColumn() {
        // Test a complete, valid column with all fields set
        ColumnMetadata validColumn = new ColumnMetadata();
        validColumn.setTable(table);
        validColumn.setColumnName("complete_column");
        validColumn.setDataType("DECIMAL(10,2)");
        validColumn.setColumnOrder(5);
        validColumn.setIsNullable(true);
        validColumn.setIsPartitionKey(false);
        validColumn.setDescription("A complete column with all fields set properly");

        Set<ConstraintViolation<ColumnMetadata>> violations = validator.validate(validColumn);
        assertTrue(violations.isEmpty(), "A complete valid column should have no validation errors");
    }
}
