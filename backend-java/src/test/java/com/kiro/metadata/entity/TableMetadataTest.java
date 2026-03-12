package com.kiro.metadata.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TableMetadata entity
 * Tests entity creation, relationships, and helper methods
 */
class TableMetadataTest {

    private TableMetadata table;
    private User owner;

    @BeforeEach
    void setUp() {
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
        table.setDescription("Test table description");
        table.setStorageFormat("PARQUET");
        table.setStorageLocation("/data/warehouse/test_table");
        table.setDataSizeBytes(1024000L);
        table.setLastAccessedAt(LocalDateTime.now());
        table.setOwner(owner);
    }

    @Test
    void testTableMetadataCreation() {
        assertNotNull(table);
        assertEquals("test_db", table.getDatabaseName());
        assertEquals("test_table", table.getTableName());
        assertEquals(TableType.TABLE, table.getTableType());
        assertEquals("Test table description", table.getDescription());
        assertEquals("PARQUET", table.getStorageFormat());
        assertEquals("/data/warehouse/test_table", table.getStorageLocation());
        assertEquals(1024000L, table.getDataSizeBytes());
        assertNotNull(table.getLastAccessedAt());
        assertEquals(owner, table.getOwner());
    }

    @Test
    void testTableTypeEnum() {
        table.setTableType(TableType.TABLE);
        assertEquals(TableType.TABLE, table.getTableType());

        table.setTableType(TableType.VIEW);
        assertEquals(TableType.VIEW, table.getTableType());

        table.setTableType(TableType.EXTERNAL);
        assertEquals(TableType.EXTERNAL, table.getTableType());
    }

    @Test
    void testAddColumn() {
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnName("test_column");

        table.addColumn(column);

        assertEquals(1, table.getColumns().size());
        assertTrue(table.getColumns().contains(column));
        assertEquals(table, column.getTable());
    }

    @Test
    void testRemoveColumn() {
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnName("test_column");

        table.addColumn(column);
        assertEquals(1, table.getColumns().size());

        table.removeColumn(column);
        assertEquals(0, table.getColumns().size());
        assertNull(column.getTable());
    }

    @Test
    void testAddToCatalog() {
        Catalog catalog = new Catalog();
        catalog.setName("test_catalog");

        table.addToCatalog(catalog);

        assertEquals(1, table.getCatalogs().size());
        assertTrue(table.getCatalogs().contains(catalog));
        assertTrue(catalog.getTables().contains(table));
    }

    @Test
    void testRemoveFromCatalog() {
        Catalog catalog = new Catalog();
        catalog.setName("test_catalog");

        table.addToCatalog(catalog);
        assertEquals(1, table.getCatalogs().size());

        table.removeFromCatalog(catalog);
        assertEquals(0, table.getCatalogs().size());
        assertFalse(catalog.getTables().contains(table));
    }

    @Test
    void testOwnerRelationship() {
        assertNotNull(table.getOwner());
        assertEquals("testuser", table.getOwner().getUsername());
        assertEquals(UserRole.DEVELOPER, table.getOwner().getRole());
    }

    @Test
    void testStorageInformation() {
        assertEquals("PARQUET", table.getStorageFormat());
        assertEquals("/data/warehouse/test_table", table.getStorageLocation());
        assertEquals(1024000L, table.getDataSizeBytes());

        // Test updating storage information
        table.setStorageFormat("ORC");
        table.setStorageLocation("/data/warehouse/new_location");
        table.setDataSizeBytes(2048000L);

        assertEquals("ORC", table.getStorageFormat());
        assertEquals("/data/warehouse/new_location", table.getStorageLocation());
        assertEquals(2048000L, table.getDataSizeBytes());
    }

    @Test
    void testUniqueConstraintFields() {
        // Test that database_name and table_name are set correctly
        // These fields form a unique constraint
        assertNotNull(table.getDatabaseName());
        assertNotNull(table.getTableName());
        assertEquals("test_db", table.getDatabaseName());
        assertEquals("test_table", table.getTableName());
    }

    @Test
    void testInheritedBaseEntityFields() {
        // Test that BaseEntity fields are accessible
        assertNotNull(table);
        
        // isDeleted should default to false
        assertFalse(table.getIsDeleted());
        
        // Test setting isDeleted
        table.setIsDeleted(true);
        assertTrue(table.getIsDeleted());
    }
}
