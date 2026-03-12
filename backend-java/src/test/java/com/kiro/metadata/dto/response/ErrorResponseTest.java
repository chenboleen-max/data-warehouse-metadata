package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorResponse DTO
 */
class ErrorResponseTest {

    @Test
    void testErrorResponseCreation() {
        LocalDateTime now = LocalDateTime.now();
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("RESOURCE_NOT_FOUND")
                .errorMessage("Table not found")
                .timestamp(now)
                .requestId("req-123")
                .status(404)
                .path("/api/v1/tables/123")
                .build();

        assertThat(response.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getErrorMessage()).isEqualTo("Table not found");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getRequestId()).isEqualTo("req-123");
    }

    @Test
    void testErrorResponseWithDetails() {
        ErrorResponse.ErrorDetail detail1 = ErrorResponse.ErrorDetail.builder()
                .field("tableName")
                .message("Table name cannot be blank")
                .rejectedValue("")
                .code("NotBlank")
                .build();

        ErrorResponse.ErrorDetail detail2 = ErrorResponse.ErrorDetail.builder()
                .field("databaseName")
                .message("Database name cannot be blank")
                .rejectedValue(null)
                .code("NotBlank")
                .build();

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Validation failed")
                .status(422)
                .path("/api/v1/tables")
                .build();
        
        response.addDetail("tableName", "Table name cannot be blank", "", "NotBlank");
        response.addDetail("databaseName", "Database name cannot be blank", null, "NotBlank");

        assertThat(response.getDetails()).hasSize(2);
        assertThat(response.getDetails().get(0).getField()).isEqualTo("tableName");
        assertThat(response.getDetails().get(1).getField()).isEqualTo("databaseName");
    }

    @Test
    void testAddDetailMethod() {
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Validation failed")
                .build();

        response.addDetail("email", "Invalid email format", "invalid-email", "Email");

        assertThat(response.getDetails()).hasSize(1);
        assertThat(response.getDetails().get(0).getField()).isEqualTo("email");
        assertThat(response.getDetails().get(0).getMessage()).isEqualTo("Invalid email format");
        assertThat(response.getDetails().get(0).getRejectedValue()).isEqualTo("invalid-email");
        assertThat(response.getDetails().get(0).getCode()).isEqualTo("Email");
    }

    @Test
    void testAddSimpleDetailMethod() {
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Validation failed")
                .build();

        response.addDetail("username", "Username is required");

        assertThat(response.getDetails()).hasSize(1);
        assertThat(response.getDetails().get(0).getField()).isEqualTo("username");
        assertThat(response.getDetails().get(0).getMessage()).isEqualTo("Username is required");
        assertThat(response.getDetails().get(0).getRejectedValue()).isNull();
        assertThat(response.getDetails().get(0).getCode()).isNull();
    }

    @Test
    void testMultipleValidationErrors() {
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Multiple validation errors")
                .status(422)
                .build();

        response.addDetail("tableName", "Table name is required");
        response.addDetail("databaseName", "Database name is required");
        response.addDetail("tableType", "Table type must be TABLE, VIEW, or EXTERNAL");

        assertThat(response.getDetails()).hasSize(3);
    }

    @Test
    void testErrorResponseWithMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("attemptedTableId", "123");
        metadata.put("userId", "user-456");
        metadata.put("timestamp", System.currentTimeMillis());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("PERMISSION_DENIED")
                .errorMessage("User does not have permission to delete this table")
                .status(403)
                .path("/api/v1/tables/123")
                .metadata(metadata)
                .build();

        assertThat(response.getMetadata()).hasSize(3);
        assertThat(response.getMetadata().get("attemptedTableId")).isEqualTo("123");
    }

    @Test
    void testDifferentErrorCodes() {
        String[] errorCodes = {
            "RESOURCE_NOT_FOUND",
            "VALIDATION_ERROR",
            "PERMISSION_DENIED",
            "INTERNAL_SERVER_ERROR",
            "DATABASE_ERROR",
            "CIRCULAR_DEPENDENCY"
        };

        for (String errorCode : errorCodes) {
            ErrorResponse response = ErrorResponse.builder()
                    .errorCode(errorCode)
                    .errorMessage("Error message")
                    .build();

            assertThat(response.getErrorCode()).isEqualTo(errorCode);
        }
    }

    @Test
    void testDifferentHttpStatuses() {
        int[] statuses = {400, 401, 403, 404, 422, 500, 503};

        for (int status : statuses) {
            ErrorResponse response = ErrorResponse.builder()
                    .errorCode("ERROR")
                    .errorMessage("Error")
                    .status(status)
                    .build();

            assertThat(response.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void testTimestampDefault() {
        LocalDateTime before = LocalDateTime.now();
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("ERROR")
                .errorMessage("Error")
                .build();

        LocalDateTime after = LocalDateTime.now();

        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBetween(before, after);
    }

    @Test
    void testErrorDetailCreation() {
        ErrorResponse.ErrorDetail detail = ErrorResponse.ErrorDetail.builder()
                .field("level")
                .message("Level must be between 1 and 5")
                .rejectedValue(6)
                .code("Range")
                .build();

        assertThat(detail.getField()).isEqualTo("level");
        assertThat(detail.getMessage()).isEqualTo("Level must be between 1 and 5");
        assertThat(detail.getRejectedValue()).isEqualTo(6);
        assertThat(detail.getCode()).isEqualTo("Range");
    }

    @Test
    void testEmptyDetails() {
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .errorMessage("An unexpected error occurred")
                .status(500)
                .build();

        assertThat(response.getDetails()).isNotNull();
        assertThat(response.getDetails()).isEmpty();
    }

    @Test
    void testBuilderDefaults() {
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("ERROR")
                .errorMessage("Message")
                .build();

        assertThat(response.getDetails()).isNotNull();
        assertThat(response.getDetails()).isEmpty();
        assertThat(response.getTimestamp()).isNotNull();
    }
}
