package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for QualityMetricsResponse DTO
 */
class QualityMetricsResponseTest {

    @Test
    void testQualityMetricsResponseCreation() {
        UUID id = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(id)
                .tableId(tableId)
                .tableName("users")
                .databaseName("analytics")
                .recordCount(1000000L)
                .nullRate(0.05)
                .updateFrequency("DAILY")
                .dataFreshnessHours(2)
                .qualityScore(95.0)
                .qualityStatus("EXCELLENT")
                .measuredAt(now)
                .createdAt(now)
                .build();

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getTableId()).isEqualTo(tableId);
        assertThat(response.getRecordCount()).isEqualTo(1000000L);
        assertThat(response.getNullRate()).isEqualTo(0.05);
        assertThat(response.getQualityScore()).isEqualTo(95.0);
    }

    @Test
    void testHighNullRateDetection() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .nullRate(0.6)
                .build();

        assertThat(response.isHighNullRate()).isTrue();
    }

    @Test
    void testLowNullRateDetection() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .nullRate(0.3)
                .build();

        assertThat(response.isHighNullRate()).isFalse();
    }

    @Test
    void testExactThresholdNullRate() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .nullRate(0.5)
                .build();

        assertThat(response.isHighNullRate()).isFalse();
    }

    @Test
    void testStaleDataDetection() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .dataFreshnessHours(200)
                .build();

        assertThat(response.isStaleData()).isTrue();
    }

    @Test
    void testFreshDataDetection() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .dataFreshnessHours(24)
                .build();

        assertThat(response.isStaleData()).isFalse();
    }

    @Test
    void testExactThresholdFreshness() {
        // 168 hours = 7 days
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .dataFreshnessHours(168)
                .build();

        assertThat(response.isStaleData()).isFalse();
    }

    @Test
    void testNullNullRateHandling() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .nullRate(null)
                .build();

        assertThat(response.isHighNullRate()).isFalse();
    }

    @Test
    void testNullFreshnessHandling() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .dataFreshnessHours(null)
                .build();

        assertThat(response.isStaleData()).isFalse();
    }

    @Test
    void testExcellentQualityMetrics() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .recordCount(5000000L)
                .nullRate(0.01)
                .updateFrequency("DAILY")
                .dataFreshnessHours(1)
                .qualityScore(98.5)
                .qualityStatus("EXCELLENT")
                .build();

        assertThat(response.isHighNullRate()).isFalse();
        assertThat(response.isStaleData()).isFalse();
        assertThat(response.getQualityStatus()).isEqualTo("EXCELLENT");
    }

    @Test
    void testPoorQualityMetrics() {
        QualityMetricsResponse response = QualityMetricsResponse.builder()
                .id(UUID.randomUUID())
                .tableId(UUID.randomUUID())
                .recordCount(100L)
                .nullRate(0.8)
                .updateFrequency("MONTHLY")
                .dataFreshnessHours(720)
                .qualityScore(25.0)
                .qualityStatus("POOR")
                .build();

        assertThat(response.isHighNullRate()).isTrue();
        assertThat(response.isStaleData()).isTrue();
        assertThat(response.getQualityStatus()).isEqualTo("POOR");
    }

    @Test
    void testDifferentUpdateFrequencies() {
        String[] frequencies = {"DAILY", "WEEKLY", "MONTHLY"};
        
        for (String frequency : frequencies) {
            QualityMetricsResponse response = QualityMetricsResponse.builder()
                    .id(UUID.randomUUID())
                    .tableId(UUID.randomUUID())
                    .updateFrequency(frequency)
                    .build();

            assertThat(response.getUpdateFrequency()).isEqualTo(frequency);
        }
    }
}
