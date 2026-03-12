package com.kiro.metadata.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QualityMetrics entity
 * Tests entity creation, validation, relationships, and constraints
 * 
 * Validates: Requirements 9.1, 9.2 (Data Quality Indicators)
 */
class QualityMetricsTest {

    private QualityMetrics qualityMetrics;
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

        // Create table
        table = new TableMetadata();
        table.setDatabaseName("test_db");
        table.setTableName("test_table");
        table.setTableType(TableType.TABLE);
        table.setOwner(owner);

        // Create quality metrics
        qualityMetrics = new QualityMetrics();
        qualityMetrics.setTable(table);
        qualityMetrics.setRecordCount(1000L);
        qualityMetrics.setNullRate(new BigDecimal("0.1500"));
        qualityMetrics.setUpdateFrequency("DAILY");
        qualityMetrics.setDataFreshnessHours(2);
        qualityMetrics.setMeasuredAt(LocalDateTime.now());
    }

    @Test
    void testQualityMetricsCreation() {
        assertNotNull(qualityMetrics);
        assertEquals(table, qualityMetrics.getTable());
        assertEquals(1000L, qualityMetrics.getRecordCount());
        assertEquals(new BigDecimal("0.1500"), qualityMetrics.getNullRate());
        assertEquals("DAILY", qualityMetrics.getUpdateFrequency());
        assertEquals(2, qualityMetrics.getDataFreshnessHours());
        assertNotNull(qualityMetrics.getMeasuredAt());
    }

    @Test
    void testMeasuredAtValidation() {
        // Valid measuredAt
        qualityMetrics.setMeasuredAt(LocalDateTime.now());
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Null measuredAt should fail validation
        qualityMetrics.setMeasuredAt(null);
        violations = validator.validate(qualityMetrics);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Measured at timestamp cannot be null")));
    }

    @Test
    void testNullRateValidation() {
        // Valid null rates (0.0 to 1.0)
        qualityMetrics.setNullRate(new BigDecimal("0.0000"));
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        qualityMetrics.setNullRate(new BigDecimal("0.5000"));
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        qualityMetrics.setNullRate(new BigDecimal("1.0000"));
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Null rate below 0.0 should fail validation
        qualityMetrics.setNullRate(new BigDecimal("-0.0001"));
        violations = validator.validate(qualityMetrics);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Null rate must be at least 0.0")));

        // Null rate above 1.0 should fail validation
        qualityMetrics.setNullRate(new BigDecimal("1.0001"));
        violations = validator.validate(qualityMetrics);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Null rate must not exceed 1.0")));
    }

    @Test
    void testNullRatePrecision() {
        // Test precision with 4 decimal places (DECIMAL(5,4))
        qualityMetrics.setNullRate(new BigDecimal("0.1234"));
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Test edge case with maximum precision
        qualityMetrics.setNullRate(new BigDecimal("0.9999"));
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Test minimum non-zero value
        qualityMetrics.setNullRate(new BigDecimal("0.0001"));
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullRateOptional() {
        // Null rate can be null (optional field)
        qualityMetrics.setNullRate(null);
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRecordCountOptional() {
        // Record count can be null (optional field)
        qualityMetrics.setRecordCount(null);
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Test with zero records
        qualityMetrics.setRecordCount(0L);
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Test with large record count
        qualityMetrics.setRecordCount(1_000_000_000L);
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUpdateFrequencyOptional() {
        // Update frequency can be null (optional field)
        qualityMetrics.setUpdateFrequency(null);
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Test various frequency values
        qualityMetrics.setUpdateFrequency("DAILY");
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        qualityMetrics.setUpdateFrequency("WEEKLY");
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        qualityMetrics.setUpdateFrequency("MONTHLY");
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDataFreshnessHoursOptional() {
        // Data freshness hours can be null (optional field)
        qualityMetrics.setDataFreshnessHours(null);
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Test with zero hours (very fresh data)
        qualityMetrics.setDataFreshnessHours(0);
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Test with large hours (stale data)
        qualityMetrics.setDataFreshnessHours(168); // 7 days
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testManyToOneTableRelationship() {
        assertNotNull(qualityMetrics.getTable());
        assertEquals(table, qualityMetrics.getTable());
        assertEquals("test_db", qualityMetrics.getTable().getDatabaseName());
        assertEquals("test_table", qualityMetrics.getTable().getTableName());
        assertEquals(TableType.TABLE, qualityMetrics.getTable().getTableType());
    }

    @Test
    void testMultipleMetricsForSameTable() {
        // Create first metrics measurement
        QualityMetrics metrics1 = new QualityMetrics();
        metrics1.setTable(table);
        metrics1.setRecordCount(1000L);
        metrics1.setNullRate(new BigDecimal("0.1000"));
        metrics1.setMeasuredAt(LocalDateTime.now().minusDays(1));

        // Create second metrics measurement (more recent)
        QualityMetrics metrics2 = new QualityMetrics();
        metrics2.setTable(table);
        metrics2.setRecordCount(1100L);
        metrics2.setNullRate(new BigDecimal("0.0900"));
        metrics2.setMeasuredAt(LocalDateTime.now());

        // Both metrics should be valid and reference the same table
        assertEquals(table, metrics1.getTable());
        assertEquals(table, metrics2.getTable());
        assertNotEquals(metrics1.getMeasuredAt(), metrics2.getMeasuredAt());
        assertTrue(metrics2.getMeasuredAt().isAfter(metrics1.getMeasuredAt()));
    }

    @Test
    void testInheritedBaseEntityFields() {
        // Test that BaseEntity fields are accessible
        assertNotNull(qualityMetrics);
        
        // isDeleted should default to false
        assertFalse(qualityMetrics.getIsDeleted());
        
        // Test setting isDeleted
        qualityMetrics.setIsDeleted(true);
        assertTrue(qualityMetrics.getIsDeleted());
    }

    @Test
    void testAllArgsConstructor() {
        // Test constructor with all arguments
        LocalDateTime measuredTime = LocalDateTime.now();
        QualityMetrics newMetrics = new QualityMetrics(
            table,
            5000L,
            new BigDecimal("0.2500"),
            "WEEKLY",
            24,
            measuredTime
        );

        assertEquals(table, newMetrics.getTable());
        assertEquals(5000L, newMetrics.getRecordCount());
        assertEquals(new BigDecimal("0.2500"), newMetrics.getNullRate());
        assertEquals("WEEKLY", newMetrics.getUpdateFrequency());
        assertEquals(24, newMetrics.getDataFreshnessHours());
        assertEquals(measuredTime, newMetrics.getMeasuredAt());
    }

    @Test
    void testNoArgsConstructor() {
        // Test no-args constructor
        QualityMetrics newMetrics = new QualityMetrics();
        
        assertNotNull(newMetrics);
        assertNull(newMetrics.getTable());
        assertNull(newMetrics.getRecordCount());
        assertNull(newMetrics.getNullRate());
        assertNull(newMetrics.getUpdateFrequency());
        assertNull(newMetrics.getDataFreshnessHours());
        assertNull(newMetrics.getMeasuredAt());
    }

    @Test
    void testCompleteValidQualityMetrics() {
        // Test a complete, valid quality metrics with all fields set
        QualityMetrics validMetrics = new QualityMetrics();
        validMetrics.setTable(table);
        validMetrics.setRecordCount(10000L);
        validMetrics.setNullRate(new BigDecimal("0.0500"));
        validMetrics.setUpdateFrequency("DAILY");
        validMetrics.setDataFreshnessHours(1);
        validMetrics.setMeasuredAt(LocalDateTime.now());

        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(validMetrics);
        assertTrue(violations.isEmpty(), "A complete valid quality metrics should have no validation errors");
    }

    @Test
    void testMinimalValidQualityMetrics() {
        // Test minimal valid quality metrics (only required fields)
        QualityMetrics minimalMetrics = new QualityMetrics();
        minimalMetrics.setTable(table);
        minimalMetrics.setMeasuredAt(LocalDateTime.now());

        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(minimalMetrics);
        assertTrue(violations.isEmpty(), "Minimal quality metrics with only required fields should be valid");
        
        assertNull(minimalMetrics.getRecordCount());
        assertNull(minimalMetrics.getNullRate());
        assertNull(minimalMetrics.getUpdateFrequency());
        assertNull(minimalMetrics.getDataFreshnessHours());
    }

    @Test
    void testHighQualityData() {
        // Test metrics for high quality data (low null rate, fresh data)
        qualityMetrics.setRecordCount(1000000L);
        qualityMetrics.setNullRate(new BigDecimal("0.0100")); // 1% null rate
        qualityMetrics.setUpdateFrequency("DAILY");
        qualityMetrics.setDataFreshnessHours(1); // Very fresh
        qualityMetrics.setMeasuredAt(LocalDateTime.now());

        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
        assertTrue(qualityMetrics.getNullRate().compareTo(new BigDecimal("0.5000")) < 0);
        assertTrue(qualityMetrics.getDataFreshnessHours() < 24);
    }

    @Test
    void testLowQualityData() {
        // Test metrics for low quality data (high null rate, stale data)
        qualityMetrics.setRecordCount(100L);
        qualityMetrics.setNullRate(new BigDecimal("0.8000")); // 80% null rate
        qualityMetrics.setUpdateFrequency("MONTHLY");
        qualityMetrics.setDataFreshnessHours(200); // Very stale (8+ days)
        qualityMetrics.setMeasuredAt(LocalDateTime.now().minusDays(8));

        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
        assertTrue(qualityMetrics.getNullRate().compareTo(new BigDecimal("0.5000")) > 0);
        assertTrue(qualityMetrics.getDataFreshnessHours() > 168); // More than 7 days
    }

    @Test
    void testZeroNullRate() {
        // Test perfect data quality (no nulls)
        qualityMetrics.setNullRate(new BigDecimal("0.0000"));
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
        assertEquals(0, qualityMetrics.getNullRate().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testMaxNullRate() {
        // Test worst data quality (all nulls)
        qualityMetrics.setNullRate(new BigDecimal("1.0000"));
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
        assertEquals(0, qualityMetrics.getNullRate().compareTo(BigDecimal.ONE));
    }

    @Test
    void testMeasuredAtTimestamps() {
        // Test various timestamp scenarios
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime lastWeek = now.minusWeeks(1);

        // Current measurement
        qualityMetrics.setMeasuredAt(now);
        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Historical measurement (yesterday)
        qualityMetrics.setMeasuredAt(yesterday);
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());

        // Historical measurement (last week)
        qualityMetrics.setMeasuredAt(lastWeek);
        violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUpdateFrequencyValues() {
        // Test all common update frequency values
        String[] frequencies = {"DAILY", "WEEKLY", "MONTHLY", "HOURLY", "REAL_TIME"};
        
        for (String frequency : frequencies) {
            qualityMetrics.setUpdateFrequency(frequency);
            Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
            assertTrue(violations.isEmpty(), "Update frequency '" + frequency + "' should be valid");
        }
    }

    @Test
    void testEmptyTable() {
        // Test metrics for an empty table
        qualityMetrics.setRecordCount(0L);
        qualityMetrics.setNullRate(new BigDecimal("0.0000")); // No nulls in empty table
        qualityMetrics.setMeasuredAt(LocalDateTime.now());

        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
        assertEquals(0L, qualityMetrics.getRecordCount());
    }

    @Test
    void testLargeTable() {
        // Test metrics for a very large table
        qualityMetrics.setRecordCount(10_000_000_000L); // 10 billion records
        qualityMetrics.setNullRate(new BigDecimal("0.0001")); // Very low null rate
        qualityMetrics.setMeasuredAt(LocalDateTime.now());

        Set<ConstraintViolation<QualityMetrics>> violations = validator.validate(qualityMetrics);
        assertTrue(violations.isEmpty());
        assertTrue(qualityMetrics.getRecordCount() > 1_000_000_000L);
    }
}
