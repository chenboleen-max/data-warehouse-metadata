package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExportStatusResponse DTO
 */
class ExportStatusResponseTest {

    @Test
    void testExportStatusResponseCreation() {
        UUID taskId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ExportStatusResponse response = ExportStatusResponse.builder()
                .taskId(taskId)
                .taskType("CSV")
                .status("COMPLETED")
                .filePath("/exports/tables_20240101.csv")
                .downloadUrl("https://example.com/download/tables_20240101.csv")
                .recordCount(1000)
                .errorMessage(null)
                .progressPercentage(100)
                .createdBy(createdBy)
                .createdByUsername("admin")
                .createdAt(now)
                .startedAt(now.plusSeconds(5))
                .completedAt(now.plusSeconds(30))
                .estimatedTimeRemainingSeconds(0L)
                .build();

        assertThat(response.getTaskId()).isEqualTo(taskId);
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getRecordCount()).isEqualTo(1000);
    }

    @Test
    void testPendingStatus() {
        ExportStatusResponse response = ExportStatusResponse.builder()
                .taskId(UUID.randomUUID())
                .taskType("JSON")
                .status("PENDING")
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(response.isPending()).isTrue();
        assertThat(response.isRunning()).isFalse();
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.isFailed()).isFalse();
    }

    @Test
    void testRunningStatus() {
        ExportStatusResponse response = ExportStatusResponse.builder()
                .taskId(UUID.randomUUID())
                .taskType("CSV")
                .status("RUNNING")
                .progressPercentage(45)
                .estimatedTimeRemainingSeconds(120L)
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();

        assertThat(response.isPending()).isFalse();
        assertThat(response.isRunning()).isTrue();
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.isFailed()).isFalse();
        assertThat(response.getProgressPercentage()).isEqualTo(45);
    }

    @Test
    void testCompletedStatus() {
        ExportStatusResponse response = ExportStatusResponse.builder()
                .taskId(UUID.randomUUID())
                .taskType("CSV")
                .status("COMPLETED")
                .filePath("/exports/data.csv")
                .downloadUrl("https://example.com/download/data.csv")
                .recordCount(5000)
                .progressPercentage(100)
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now().plusMinutes(5))
                .build();

        assertThat(response.isPending()).isFalse();
        assertThat(response.isRunning()).isFalse();
        assertThat(response.isCompleted()).isTrue();
        assertThat(response.isFailed()).isFalse();
        assertThat(response.getFilePath()).isNotNull();
        assertThat(response.getDownloadUrl()).isNotNull();
    }

    @Test
    void testFailedStatus() {
        ExportStatusResponse response = ExportStatusResponse.builder()
                .taskId(UUID.randomUUID())
                .taskType("JSON")
                .status("FAILED")
                .errorMessage("Database connection timeout")
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now().plusSeconds(10))
                .build();

        assertThat(response.isPending()).isFalse();
        assertThat(response.isRunning()).isFalse();
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.isFailed()).isTrue();
        assertThat(response.getErrorMessage()).isNotNull();
    }

    @Test
    void testProgressTracking() {
        ExportStatusResponse response = ExportStatusResponse.builder()
                .taskId(UUID.randomUUID())
                .taskType("CSV")
                .status("RUNNING")
                .progressPercentage(75)
                .estimatedTimeRemainingSeconds(60L)
                .createdBy(UUID.randomUUID())
                .build();

        assertThat(response.getProgressPercentage()).isEqualTo(75);
        assertThat(response.getEstimatedTimeRemainingSeconds()).isEqualTo(60L);
    }

    @Test
    void testLargeExport() {
        // Requirement 12.4: Async export for > 10000 records
        ExportStatusResponse response = ExportStatusResponse.builder()
                .taskId(UUID.randomUUID())
                .taskType("CSV")
                .status("COMPLETED")
                .recordCount(50000)
                .filePath("/exports/large_export.csv")
                .downloadUrl("https://example.com/download/large_export.csv")
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now().plusMinutes(10))
                .build();

        assertThat(response.getRecordCount()).isGreaterThan(10000);
        assertThat(response.isCompleted()).isTrue();
    }

    @Test
    void testDifferentExportFormats() {
        String[] formats = {"CSV", "JSON"};
        
        for (String format : formats) {
            ExportStatusResponse response = ExportStatusResponse.builder()
                    .taskId(UUID.randomUUID())
                    .taskType(format)
                    .status("COMPLETED")
                    .createdBy(UUID.randomUUID())
                    .build();

            assertThat(response.getTaskType()).isEqualTo(format);
        }
    }

    @Test
    void testStatusHelperMethods() {
        ExportStatusResponse pending = ExportStatusResponse.builder()
                .status("PENDING")
                .build();
        
        ExportStatusResponse running = ExportStatusResponse.builder()
                .status("RUNNING")
                .build();
        
        ExportStatusResponse completed = ExportStatusResponse.builder()
                .status("COMPLETED")
                .build();
        
        ExportStatusResponse failed = ExportStatusResponse.builder()
                .status("FAILED")
                .build();

        assertThat(pending.isPending()).isTrue();
        assertThat(running.isRunning()).isTrue();
        assertThat(completed.isCompleted()).isTrue();
        assertThat(failed.isFailed()).isTrue();
    }
}
