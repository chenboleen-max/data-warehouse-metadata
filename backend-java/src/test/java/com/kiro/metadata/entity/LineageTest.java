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
 * Unit tests for Lineage entity
 * Tests entity creation, validation, and relationships
 * 
 * Validates: Requirements 3.1, 3.5 (Data Lineage Management)
 */
class LineageTest {

    private Lineage lineage;
    private TableMetadata sourceTable;
    private TableMetadata targetTable;
    private User owner;
    private User creator;
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Initialize validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create owner user
        owner = new User();
        owner.setUsername("tableowner");
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("hashedpassword");
        owner.setRole(UserRole.DEVELOPER);
        owner.setIsActive(true);

        // Create creator user
        creator = new User();
        creator.setUsername("lineagecreator");
        creator.setEmail("creator@example.com");
        creator.setPasswordHash("hashedpassword");
        creator.setRole(UserRole.DEVELOPER);
        creator.setIsActive(true);

        // Create source table
        sourceTable = new TableMetadata();
        sourceTable.setDatabaseName("source_db");
        sourceTable.setTableName("source_table");
        sourceTable.setTableType(TableType.TABLE);
        sourceTable.setOwner(owner);

        // Create target table
        targetTable = new TableMetadata();
        targetTable.setDatabaseName("target_db");
        targetTable.setTableName("target_table");
        targetTable.setTableType(TableType.TABLE);
        targetTable.setOwner(owner);

        // Create lineage
        lineage = new Lineage();
        lineage.setSourceTable(sourceTable);
        lineage.setTargetTable(targetTable);
        lineage.setLineageType(LineageType.DIRECT);
        lineage.setTransformationLogic("INSERT INTO target_table SELECT * FROM source_table");
        lineage.setCreatedBy(creator);
    }

    @Test
    void testLineageCreation() {
        assertNotNull(lineage);
        assertEquals(sourceTable, lineage.getSourceTable());
        assertEquals(targetTable, lineage.getTargetTable());
        assertEquals(LineageType.DIRECT, lineage.getLineageType());
        assertEquals("INSERT INTO target_table SELECT * FROM source_table", lineage.getTransformationLogic());
        assertEquals(creator, lineage.getCreatedBy());
    }

    @Test
    void testSourceTableRelationship() {
        assertNotNull(lineage.getSourceTable());
        assertEquals(sourceTable, lineage.getSourceTable());
        assertEquals("source_db", lineage.getSourceTable().getDatabaseName());
        assertEquals("source_table", lineage.getSourceTable().getTableName());
    }

    @Test
    void testTargetTableRelationship() {
        assertNotNull(lineage.getTargetTable());
        assertEquals(targetTable, lineage.getTargetTable());
        assertEquals("target_db", lineage.getTargetTable().getDatabaseName());
        assertEquals("target_table", lineage.getTargetTable().getTableName());
    }

    @Test
    void testCreatedByRelationship() {
        assertNotNull(lineage.getCreatedBy());
        assertEquals(creator, lineage.getCreatedBy());
        assertEquals("lineagecreator", lineage.getCreatedBy().getUsername());
    }

    @Test
    void testLineageTypeEnum() {
        // Test DIRECT lineage type
        lineage.setLineageType(LineageType.DIRECT);
        assertEquals(LineageType.DIRECT, lineage.getLineageType());

        // Test INDIRECT lineage type
        lineage.setLineageType(LineageType.INDIRECT);
        assertEquals(LineageType.INDIRECT, lineage.getLineageType());
    }

    @Test
    void testLineageTypeValidation() {
        // Valid lineage with type
        lineage.setLineageType(LineageType.DIRECT);
        Set<ConstraintViolation<Lineage>> violations = validator.validate(lineage);
        assertTrue(violations.isEmpty());

        // Null lineage type should fail validation
        lineage.setLineageType(null);
        violations = validator.validate(lineage);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Lineage type cannot be null")));
    }

    @Test
    void testSourceTableValidation() {
        // Valid lineage with source table
        lineage.setSourceTable(sourceTable);
        Set<ConstraintViolation<Lineage>> violations = validator.validate(lineage);
        assertTrue(violations.isEmpty());

        // Null source table should fail validation
        lineage.setSourceTable(null);
        violations = validator.validate(lineage);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Source table cannot be null")));
    }

    @Test
    void testTargetTableValidation() {
        // Valid lineage with target table
        lineage.setTargetTable(targetTable);
        Set<ConstraintViolation<Lineage>> violations = validator.validate(lineage);
        assertTrue(violations.isEmpty());

        // Null target table should fail validation
        lineage.setTargetTable(null);
        violations = validator.validate(lineage);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Target table cannot be null")));
    }

    @Test
    void testTransformationLogicField() {
        // Test with transformation logic
        String sql = "INSERT INTO target SELECT id, name FROM source WHERE active = true";
        lineage.setTransformationLogic(sql);
        assertEquals(sql, lineage.getTransformationLogic());

        // Test with null transformation logic (should be allowed)
        lineage.setTransformationLogic(null);
        assertNull(lineage.getTransformationLogic());
        Set<ConstraintViolation<Lineage>> violations = validator.validate(lineage);
        assertTrue(violations.isEmpty());

        // Test with empty transformation logic (should be allowed)
        lineage.setTransformationLogic("");
        assertEquals("", lineage.getTransformationLogic());
    }

    @Test
    void testLargeTransformationLogic() {
        // Test with large SQL query (TEXT type should support this)
        StringBuilder largeSql = new StringBuilder();
        largeSql.append("INSERT INTO target_table\n");
        largeSql.append("SELECT\n");
        for (int i = 1; i <= 100; i++) {
            largeSql.append("  column").append(i).append(",\n");
        }
        largeSql.append("  last_column\n");
        largeSql.append("FROM source_table\n");
        largeSql.append("WHERE condition = true\n");
        largeSql.append("AND another_condition IS NOT NULL");

        lineage.setTransformationLogic(largeSql.toString());
        assertEquals(largeSql.toString(), lineage.getTransformationLogic());
        
        Set<ConstraintViolation<Lineage>> violations = validator.validate(lineage);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDirectLineageType() {
        // Create a direct lineage relationship
        Lineage directLineage = new Lineage();
        directLineage.setSourceTable(sourceTable);
        directLineage.setTargetTable(targetTable);
        directLineage.setLineageType(LineageType.DIRECT);
        directLineage.setTransformationLogic("INSERT INTO target SELECT * FROM source");
        directLineage.setCreatedBy(creator);

        assertEquals(LineageType.DIRECT, directLineage.getLineageType());
        Set<ConstraintViolation<Lineage>> violations = validator.validate(directLineage);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testIndirectLineageType() {
        // Create an indirect lineage relationship
        Lineage indirectLineage = new Lineage();
        indirectLineage.setSourceTable(sourceTable);
        indirectLineage.setTargetTable(targetTable);
        indirectLineage.setLineageType(LineageType.INDIRECT);
        indirectLineage.setTransformationLogic(
            "Data flows through temp_table_1 and temp_table_2 with aggregations"
        );
        indirectLineage.setCreatedBy(creator);

        assertEquals(LineageType.INDIRECT, indirectLineage.getLineageType());
        Set<ConstraintViolation<Lineage>> violations = validator.validate(indirectLineage);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleLineagesFromSameSource() {
        // Create multiple lineages from the same source table
        TableMetadata target1 = new TableMetadata();
        target1.setDatabaseName("db1");
        target1.setTableName("target1");
        target1.setTableType(TableType.TABLE);
        target1.setOwner(owner);

        TableMetadata target2 = new TableMetadata();
        target2.setDatabaseName("db2");
        target2.setTableName("target2");
        target2.setTableType(TableType.TABLE);
        target2.setOwner(owner);

        Lineage lineage1 = new Lineage();
        lineage1.setSourceTable(sourceTable);
        lineage1.setTargetTable(target1);
        lineage1.setLineageType(LineageType.DIRECT);
        lineage1.setCreatedBy(creator);

        Lineage lineage2 = new Lineage();
        lineage2.setSourceTable(sourceTable);
        lineage2.setTargetTable(target2);
        lineage2.setLineageType(LineageType.DIRECT);
        lineage2.setCreatedBy(creator);

        assertNotEquals(lineage1.getTargetTable(), lineage2.getTargetTable());
        assertEquals(lineage1.getSourceTable(), lineage2.getSourceTable());
    }

    @Test
    void testMultipleLineagesToSameTarget() {
        // Create multiple lineages to the same target table
        TableMetadata source1 = new TableMetadata();
        source1.setDatabaseName("db1");
        source1.setTableName("source1");
        source1.setTableType(TableType.TABLE);
        source1.setOwner(owner);

        TableMetadata source2 = new TableMetadata();
        source2.setDatabaseName("db2");
        source2.setTableName("source2");
        source2.setTableType(TableType.TABLE);
        source2.setOwner(owner);

        Lineage lineage1 = new Lineage();
        lineage1.setSourceTable(source1);
        lineage1.setTargetTable(targetTable);
        lineage1.setLineageType(LineageType.DIRECT);
        lineage1.setCreatedBy(creator);

        Lineage lineage2 = new Lineage();
        lineage2.setSourceTable(source2);
        lineage2.setTargetTable(targetTable);
        lineage2.setLineageType(LineageType.DIRECT);
        lineage2.setCreatedBy(creator);

        assertNotEquals(lineage1.getSourceTable(), lineage2.getSourceTable());
        assertEquals(lineage1.getTargetTable(), lineage2.getTargetTable());
    }

    @Test
    void testInheritedBaseEntityFields() {
        // Test that BaseEntity fields are accessible
        assertNotNull(lineage);
        
        // isDeleted should default to false
        assertFalse(lineage.getIsDeleted());
        
        // Test setting isDeleted
        lineage.setIsDeleted(true);
        assertTrue(lineage.getIsDeleted());
    }

    @Test
    void testAllArgsConstructor() {
        // Test constructor with all arguments
        Lineage newLineage = new Lineage(
            sourceTable,
            targetTable,
            LineageType.INDIRECT,
            "Complex ETL transformation",
            creator
        );

        assertEquals(sourceTable, newLineage.getSourceTable());
        assertEquals(targetTable, newLineage.getTargetTable());
        assertEquals(LineageType.INDIRECT, newLineage.getLineageType());
        assertEquals("Complex ETL transformation", newLineage.getTransformationLogic());
        assertEquals(creator, newLineage.getCreatedBy());
    }

    @Test
    void testNoArgsConstructor() {
        // Test no-args constructor
        Lineage newLineage = new Lineage();
        
        assertNotNull(newLineage);
        assertNull(newLineage.getSourceTable());
        assertNull(newLineage.getTargetTable());
        assertNull(newLineage.getLineageType());
        assertNull(newLineage.getTransformationLogic());
        assertNull(newLineage.getCreatedBy());
    }

    @Test
    void testCompleteValidLineage() {
        // Test a complete, valid lineage with all fields set
        Lineage validLineage = new Lineage();
        validLineage.setSourceTable(sourceTable);
        validLineage.setTargetTable(targetTable);
        validLineage.setLineageType(LineageType.DIRECT);
        validLineage.setTransformationLogic(
            "INSERT INTO target_db.target_table\n" +
            "SELECT id, name, created_at\n" +
            "FROM source_db.source_table\n" +
            "WHERE is_active = true"
        );
        validLineage.setCreatedBy(creator);

        Set<ConstraintViolation<Lineage>> violations = validator.validate(validLineage);
        assertTrue(violations.isEmpty(), "A complete valid lineage should have no validation errors");
    }

    @Test
    void testLineageWithViewAsSource() {
        // Test lineage with a view as source
        TableMetadata viewSource = new TableMetadata();
        viewSource.setDatabaseName("view_db");
        viewSource.setTableName("source_view");
        viewSource.setTableType(TableType.VIEW);
        viewSource.setOwner(owner);

        Lineage viewLineage = new Lineage();
        viewLineage.setSourceTable(viewSource);
        viewLineage.setTargetTable(targetTable);
        viewLineage.setLineageType(LineageType.INDIRECT);
        viewLineage.setTransformationLogic("INSERT INTO target SELECT * FROM source_view");
        viewLineage.setCreatedBy(creator);

        assertEquals(TableType.VIEW, viewLineage.getSourceTable().getTableType());
        Set<ConstraintViolation<Lineage>> violations = validator.validate(viewLineage);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLineageWithExternalTableAsTarget() {
        // Test lineage with an external table as target
        TableMetadata externalTarget = new TableMetadata();
        externalTarget.setDatabaseName("external_db");
        externalTarget.setTableName("external_table");
        externalTarget.setTableType(TableType.EXTERNAL);
        externalTarget.setOwner(owner);

        Lineage externalLineage = new Lineage();
        externalLineage.setSourceTable(sourceTable);
        externalLineage.setTargetTable(externalTarget);
        externalLineage.setLineageType(LineageType.DIRECT);
        externalLineage.setTransformationLogic("EXPORT data to external storage");
        externalLineage.setCreatedBy(creator);

        assertEquals(TableType.EXTERNAL, externalLineage.getTargetTable().getTableType());
        Set<ConstraintViolation<Lineage>> violations = validator.validate(externalLineage);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLineageTypeEnumValues() {
        // Verify all enum values exist
        LineageType[] types = LineageType.values();
        assertEquals(2, types.length);
        
        // Verify specific values
        assertEquals(LineageType.DIRECT, LineageType.valueOf("DIRECT"));
        assertEquals(LineageType.INDIRECT, LineageType.valueOf("INDIRECT"));
    }

    @Test
    void testCreatedByValidation() {
        // Valid lineage with createdBy
        lineage.setCreatedBy(creator);
        Set<ConstraintViolation<Lineage>> violations = validator.validate(lineage);
        assertTrue(violations.isEmpty());

        // Null createdBy should fail validation
        lineage.setCreatedBy(null);
        violations = validator.validate(lineage);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Created by user cannot be null")));
    }
}
