package com.kiro.metadata.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExportTask entity
 * Tests entity creation, validation, relationships, and lifecycle
 * 
 * Validates: Requirements 12.4 (Asynchronous Export Functionality)
 */
class ExportTaskTest {

    private ExportTask exportTask;
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
        creator.setRole(UserRole.DEVELOPER);
        creator.setIsActive(true);

        // Create export task
        exportTask = new ExportTask();
        exportTask.setTaskType(ExportType.CSV);
        exportTask.setFilters("{\"database\":\"prod\",\"dateFrom\":\"2024-01-01\"}");
        exportTask.setStatus(TaskStatus.PENDING);
        exportTask.setCreatedBy(creator);
    }

    @Test
    void testExportTaskCreation() {
        assertNotNull(exportTask);
        assertEquals(ExportType.CSV, exportTask.getTaskType());
        assertEquals("{\"database\":\"prod\",\"dateFrom\":\"2024-01-01\"}", exportTask.getFilters());
        assertEquals(TaskStatus.PENDING, exportTask.getStatus());
        assertEquals(creator, exportTask.getCreatedBy());
        assertNull(exportTask.getFilePath());
        assertNull(exportTask.getRecordCount());
        assertNull(exportTask.getErrorMessage());
        assertNull(exportTask.getStartedAt());
        assertNull(exportTask.getCompletedAt());
    }

    @Test
    void testTaskTypeEnumValues() {
        // Test CSV type
        exportTask.setTaskType(ExportType.CSV);
        assertEquals(ExportType.CSV, exportTask.getTaskType());
        Set<ConstraintViolation<ExportTask>> violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());

        // Test JSON type
        exportTask.setTaskType(ExportType.JSON);
        assertEquals(ExportType.JSON, exportTask.getTaskType());
        violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testStatusEnumValues() {
        // Test PENDING status
        exportTask.setStatus(TaskStatus.PENDING);
        assertEquals(TaskStatus.PENDING, exportTask.getStatus());
        Set<ConstraintViolation<ExportTask>> violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());

        // Test RUNNING status
        exportTask.setStatus(TaskStatus.RUNNING);
        assertEquals(TaskStatus.RUNNING, exportTask.getStatus());
        violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());

        // Test COMPLETED status
        exportTask.setStatus(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, exportTask.getStatus());
        violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());

        // Test FAILED status
        exportTask.setStatus(TaskStatus.FAILED);
        assertEquals(TaskStatus.FAILED, exportTask.getStatus());
        violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFiltersJsonField() {
        // Test simple JSON filter
        String simpleFilter = "{\"database\":\"test\"}";
        exportTask.setFilters(simpleFilter);
        assertEquals(simpleFilter, exportTask.getFilters());

        // Test complex JSON filter
        String complexFilter = "{\"database\":\"prod\",\"dateFrom\":\"2024-01-01\",\"dateTo\":\"2024-12-31\",\"tables\":[\"users\",\"orders\"]}";
        exportTask.setFilters(complexFilter);
        assertEquals(complexFilter, exportTask.getFilters());

        // Test null filter (should be allowed)
        exportTask.setFilters(null);
        assertNull(exportTask.getFilters());
        Set<ConstraintViolation<ExportTask>> violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFilePathValidation() {
        // Valid file path
        exportTask.setFilePath("/exports/2024/01/export_123.csv");
        Set<ConstraintViolation<ExportTask>> violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());

        // Test file path within limit (500 characters)
        String validPath = "/exports/" + "a".repeat(486) + ".csv";
        exportTask.setFilePath(validPath);
        violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());

        // Test file path exceeding limit
        String invalidPath = "/exports/" + "a".repeat(492) + ".csv";
        exportTask.setFilePath(invalidPath);
        violations = validator.validate(exportTask);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("File path must not exceed 500 characters")));

        // Null file path should be allowed (task not completed yet)
        exportTask.setFilePath(null);
        violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRecordCount() {
        // Test positive record count
        exportTask.setRecordCount(1000);
        assertEquals(1000, exportTask.getRecordCount());

        // Test zero record count
        exportTask.setRecordCount(0);
        assertEquals(0, exportTask.getRecordCount());

        // Test large record count
        exportTask.setRecordCount(1000000);
        assertEquals(1000000, exportTask.getRecordCount());

        // Null record count should be allowed (task not completed yet)
        exportTask.setRecordCount(null);
        assertNull(exportTask.getRecordCount());
    }

    @Test
    void testErrorMessage() {
        // Test short error message
        String shortError = "Connection timeout";
        exportTask.setErrorMessage(shortError);
        assertEquals(shortError, exportTask.getErrorMessage());

        // Test long error message with stack trace
        String longError = "java.sql.SQLException: Connection timeout\n" +
            "at com.kiro.metadata.service.ExportService.export(ExportService.java:123)\n" +
            "at com.kiro.metadata.controller.ExportController.createExport(ExportController.java:45)\n" +
            "Caused by: java.net.SocketTimeoutException: Read timed out\n" +
            "at java.net.SocketInputStream.socketRead0(Native Method)";
        exportTask.setErrorMessage(longError);
        assertEquals(longError, exportTask.getErrorMessage());

        // Null error message should be allowed (no error)
        exportTask.setErrorMessage(null);
        assertNull(exportTask.getErrorMessage());
    }

    @Test
    void testCreatedByRelationship() {
        assertNotNull(exportTask.getCreatedBy());
        assertEquals(creator, exportTask.getCreatedBy());
        assertEquals("testuser", exportTask.getCreatedBy().getUsername());
        assertEquals(UserRole.DEVELOPER, exportTask.getCreatedBy().getRole());
    }

    @Test
    void testTimestampFields() {
        LocalDateTime now = LocalDateTime.now();
        
        // Test startedAt
        exportTask.setStartedAt(now);
        assertEquals(now, exportTask.getStartedAt());
        
        // Test completedAt
        exportTask.setCompletedAt(now.plusMinutes(5));
        assertEquals(now.plusMinutes(5), exportTask.getCompletedAt());
        
        // Null timestamps should be allowed
        exportTask.setStartedAt(null);
        exportTask.setCompletedAt(null);
        assertNull(exportTask.getStartedAt());
        assertNull(exportTask.getCompletedAt());
    }

    @Test
    void testPendingTaskLifecycle() {
        // Initial state: PENDING
        exportTask.setStatus(TaskStatus.PENDING);
        assertNull(exportTask.getStartedAt());
        assertNull(exportTask.getCompletedAt());
        assertNull(exportTask.getFilePath());
        assertNull(exportTask.getRecordCount());
        assertNull(exportTask.getErrorMessage());
        
        assertEquals(TaskStatus.PENDING, exportTask.getStatus());
    }

    @Test
    void testRunningTaskLifecycle() {
        // Transition to RUNNING
        exportTask.setStatus(TaskStatus.RUNNING);
        exportTask.setStartedAt(LocalDateTime.now());
        
        assertEquals(TaskStatus.RUNNING, exportTask.getStatus());
        assertNotNull(exportTask.getStartedAt());
        assertNull(exportTask.getCompletedAt());
        assertNull(exportTask.getFilePath());
        assertNull(exportTask.getRecordCount());
    }

    @Test
    void testCompletedTaskLifecycle() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(5);
        
        // Transition to COMPLETED
        exportTask.setStatus(TaskStatus.COMPLETED);
        exportTask.setStartedAt(startTime);
        exportTask.setCompletedAt(endTime);
        exportTask.setFilePath("/exports/2024/01/export_123.csv");
        exportTask.setRecordCount(5000);
        
        assertEquals(TaskStatus.COMPLETED, exportTask.getStatus());
        assertEquals(startTime, exportTask.getStartedAt());
        assertEquals(endTime, exportTask.getCompletedAt());
        assertEquals("/exports/2024/01/export_123.csv", exportTask.getFilePath());
        assertEquals(5000, exportTask.getRecordCount());
        assertNull(exportTask.getErrorMessage());
    }

    @Test
    void testFailedTaskLifecycle() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(2);
        
        // Transition to FAILED
        exportTask.setStatus(TaskStatus.FAILED);
        exportTask.setStartedAt(startTime);
        exportTask.setCompletedAt(endTime);
        exportTask.setErrorMessage("Database connection failed: timeout after 30 seconds");
        
        assertEquals(TaskStatus.FAILED, exportTask.getStatus());
        assertEquals(startTime, exportTask.getStartedAt());
        assertEquals(endTime, exportTask.getCompletedAt());
        assertNotNull(exportTask.getErrorMessage());
        assertTrue(exportTask.getErrorMessage().contains("timeout"));
        assertNull(exportTask.getFilePath());
        assertNull(exportTask.getRecordCount());
    }

    @Test
    void testCsvExportTask() {
        exportTask.setTaskType(ExportType.CSV);
        exportTask.setFilters("{\"database\":\"analytics\",\"tables\":[\"events\",\"users\"]}");
        exportTask.setStatus(TaskStatus.COMPLETED);
        exportTask.setFilePath("/exports/analytics_export.csv");
        exportTask.setRecordCount(10000);
        
        assertEquals(ExportType.CSV, exportTask.getTaskType());
        assertEquals(TaskStatus.COMPLETED, exportTask.getStatus());
        assertTrue(exportTask.getFilePath().endsWith(".csv"));
        assertEquals(10000, exportTask.getRecordCount());
    }

    @Test
    void testJsonExportTask() {
        exportTask.setTaskType(ExportType.JSON);
        exportTask.setFilters("{\"database\":\"production\",\"dateRange\":\"last_30_days\"}");
        exportTask.setStatus(TaskStatus.COMPLETED);
        exportTask.setFilePath("/exports/production_export.json");
        exportTask.setRecordCount(25000);
        
        assertEquals(ExportType.JSON, exportTask.getTaskType());
        assertEquals(TaskStatus.COMPLETED, exportTask.getStatus());
        assertTrue(exportTask.getFilePath().endsWith(".json"));
        assertEquals(25000, exportTask.getRecordCount());
    }

    @Test
    void testInheritedBaseEntityFields() {
        // Test that BaseEntity fields are accessible
        assertNotNull(exportTask);
        
        // isDeleted should default to false
        assertFalse(exportTask.getIsDeleted());
        
        // Test setting isDeleted
        exportTask.setIsDeleted(true);
        assertTrue(exportTask.getIsDeleted());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        
        // Test constructor with all arguments
        ExportTask newTask = new ExportTask(
            ExportType.JSON,
            "{\"database\":\"test\"}",
            TaskStatus.PENDING,
            null,
            null,
            null,
            creator,
            null,
            null
        );
        
        assertEquals(ExportType.JSON, newTask.getTaskType());
        assertEquals("{\"database\":\"test\"}", newTask.getFilters());
        assertEquals(TaskStatus.PENDING, newTask.getStatus());
        assertNull(newTask.getFilePath());
        assertNull(newTask.getRecordCount());
        assertNull(newTask.getErrorMessage());
        assertEquals(creator, newTask.getCreatedBy());
        assertNull(newTask.getStartedAt());
        assertNull(newTask.getCompletedAt());
    }

    @Test
    void testNoArgsConstructor() {
        // Test no-args constructor
        ExportTask newTask = new ExportTask();
        
        assertNotNull(newTask);
        assertNull(newTask.getTaskType());
        assertNull(newTask.getFilters());
        assertNull(newTask.getStatus());
        assertNull(newTask.getFilePath());
        assertNull(newTask.getRecordCount());
        assertNull(newTask.getErrorMessage());
        assertNull(newTask.getCreatedBy());
        assertNull(newTask.getStartedAt());
        assertNull(newTask.getCompletedAt());
    }

    @Test
    void testCompleteValidExportTask() {
        // Test a complete, valid export task with all required fields
        ExportTask validTask = new ExportTask();
        validTask.setTaskType(ExportType.CSV);
        validTask.setFilters("{\"database\":\"prod\"}");
        validTask.setStatus(TaskStatus.PENDING);
        validTask.setCreatedBy(creator);
        
        Set<ConstraintViolation<ExportTask>> violations = validator.validate(validTask);
        assertTrue(violations.isEmpty(), "A complete valid export task should have no validation errors");
    }

    @Test
    void testMultipleExportTasksForSameUser() {
        // Create multiple export tasks for the same user
        ExportTask task1 = new ExportTask();
        task1.setTaskType(ExportType.CSV);
        task1.setStatus(TaskStatus.COMPLETED);
        task1.setCreatedBy(creator);
        task1.setRecordCount(1000);
        
        ExportTask task2 = new ExportTask();
        task2.setTaskType(ExportType.JSON);
        task2.setStatus(TaskStatus.RUNNING);
        task2.setCreatedBy(creator);
        
        ExportTask task3 = new ExportTask();
        task3.setTaskType(ExportType.CSV);
        task3.setStatus(TaskStatus.FAILED);
        task3.setCreatedBy(creator);
        task3.setErrorMessage("Export failed");
        
        // Verify all tasks belong to the same user
        assertEquals(creator, task1.getCreatedBy());
        assertEquals(creator, task2.getCreatedBy());
        assertEquals(creator, task3.getCreatedBy());
        
        // Verify different statuses
        assertEquals(TaskStatus.COMPLETED, task1.getStatus());
        assertEquals(TaskStatus.RUNNING, task2.getStatus());
        assertEquals(TaskStatus.FAILED, task3.getStatus());
    }

    @Test
    void testExportTaskWithComplexFilters() {
        // Test export task with complex filter JSON
        String complexFilters = "{"
            + "\"database\":\"analytics\","
            + "\"tables\":[\"events\",\"users\",\"sessions\"],"
            + "\"dateRange\":{"
            + "\"from\":\"2024-01-01\","
            + "\"to\":\"2024-12-31\""
            + "},"
            + "\"conditions\":{"
            + "\"status\":\"active\","
            + "\"region\":[\"US\",\"EU\",\"APAC\"]"
            + "}"
            + "}";
        
        exportTask.setFilters(complexFilters);
        assertEquals(complexFilters, exportTask.getFilters());
        
        Set<ConstraintViolation<ExportTask>> violations = validator.validate(exportTask);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testExportTaskDuration() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 5, 30);
        
        exportTask.setStartedAt(startTime);
        exportTask.setCompletedAt(endTime);
        
        assertNotNull(exportTask.getStartedAt());
        assertNotNull(exportTask.getCompletedAt());
        assertTrue(exportTask.getCompletedAt().isAfter(exportTask.getStartedAt()));
    }

    @Test
    void testExportTaskWithEmptyRecordCount() {
        // Test export task that completes but finds no records
        exportTask.setStatus(TaskStatus.COMPLETED);
        exportTask.setRecordCount(0);
        exportTask.setFilePath("/exports/empty_export.csv");
        exportTask.setStartedAt(LocalDateTime.now());
        exportTask.setCompletedAt(LocalDateTime.now().plusSeconds(1));
        
        assertEquals(TaskStatus.COMPLETED, exportTask.getStatus());
        assertEquals(0, exportTask.getRecordCount());
        assertNotNull(exportTask.getFilePath());
    }

    @Test
    void testTaskStatusEnum() {
        // Verify all TaskStatus enum values
        TaskStatus[] statuses = TaskStatus.values();
        assertEquals(4, statuses.length);
        
        assertTrue(java.util.Arrays.asList(statuses).contains(TaskStatus.PENDING));
        assertTrue(java.util.Arrays.asList(statuses).contains(TaskStatus.RUNNING));
        assertTrue(java.util.Arrays.asList(statuses).contains(TaskStatus.COMPLETED));
        assertTrue(java.util.Arrays.asList(statuses).contains(TaskStatus.FAILED));
    }

    @Test
    void testExportTypeEnum() {
        // Verify all ExportType enum values
        ExportType[] types = ExportType.values();
        assertEquals(2, types.length);
        
        assertTrue(java.util.Arrays.asList(types).contains(ExportType.CSV));
        assertTrue(java.util.Arrays.asList(types).contains(ExportType.JSON));
    }

    @Test
    void testEnumStringRepresentation() {
        // Test that enums are stored as strings (not ordinals)
        assertEquals("PENDING", TaskStatus.PENDING.name());
        assertEquals("RUNNING", TaskStatus.RUNNING.name());
        assertEquals("COMPLETED", TaskStatus.COMPLETED.name());
        assertEquals("FAILED", TaskStatus.FAILED.name());
        
        assertEquals("CSV", ExportType.CSV.name());
        assertEquals("JSON", ExportType.JSON.name());
    }
}
