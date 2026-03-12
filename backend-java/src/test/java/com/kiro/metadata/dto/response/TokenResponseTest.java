package com.kiro.metadata.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TokenResponse DTO
 * 
 * Validates: Requirements 6.2 (User Authentication)
 */
@DisplayName("TokenResponse Tests")
class TokenResponseTest {

    @Test
    @DisplayName("Should create TokenResponse with all fields")
    void testCreateTokenResponseWithAllFields() {
        // Given
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...refresh";
        String tokenType = "Bearer";
        Long expiresIn = 3600L;

        // When
        TokenResponse response = new TokenResponse(accessToken, refreshToken, tokenType, expiresIn);

        // Then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("Should create TokenResponse with default tokenType")
    void testCreateTokenResponseWithDefaultTokenType() {
        // Given
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...refresh";
        Long expiresIn = 3600L;

        // When
        TokenResponse response = new TokenResponse(accessToken, refreshToken, expiresIn);

        // Then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("Should create empty TokenResponse with no-args constructor")
    void testNoArgsConstructor() {
        // When
        TokenResponse response = new TokenResponse();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isNull();
    }

    @Test
    @DisplayName("Should allow setting fields via setters")
    void testSetters() {
        // Given
        TokenResponse response = new TokenResponse();
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String tokenType = "Custom";
        Long expiresIn = 7200L;

        // When
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType(tokenType);
        response.setExpiresIn(expiresIn);

        // Then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void testEqualsAndHashCode() {
        // Given
        TokenResponse response1 = new TokenResponse("access", "refresh", "Bearer", 3600L);
        TokenResponse response2 = new TokenResponse("access", "refresh", "Bearer", 3600L);
        TokenResponse response3 = new TokenResponse("different", "refresh", "Bearer", 3600L);

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).hasSameHashCodeAs(response2);
        assertThat(response1).isNotEqualTo(response3);
    }

    @Test
    @DisplayName("Should support toString")
    void testToString() {
        // Given
        TokenResponse response = new TokenResponse("access", "refresh", "Bearer", 3600L);

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("accessToken");
        assertThat(toString).contains("refreshToken");
        assertThat(toString).contains("tokenType");
        assertThat(toString).contains("expiresIn");
    }
}
