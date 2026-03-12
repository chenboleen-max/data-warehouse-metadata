package com.kiro.metadata.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtTokenProvider
 */
@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "test-secret-key-for-jwt-token-provider-must-be-at-least-256-bits-long";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days
    private static final String TEST_ISSUER = "test-issuer";
    private static final String TEST_AUDIENCE = "test-audience";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Set configuration properties using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "issuer", TEST_ISSUER);
        ReflectionTestUtils.setField(jwtTokenProvider, "audience", TEST_AUDIENCE);
    }

    @Test
    @DisplayName("Should generate valid access token")
    void shouldGenerateValidAccessToken() {
        // When
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void shouldGenerateValidRefreshToken() {
        // When
        String token = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "not-a-jwt-token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject empty token")
    void shouldRejectEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should extract claims from token")
    void shouldExtractClaimsFromToken() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);

        // When
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(TEST_USERNAME);
        assertThat(claims.getIssuer()).isEqualTo(TEST_ISSUER);
        assertThat(claims.getAudience()).contains(TEST_AUDIENCE);
        assertThat(claims.get("type")).isEqualTo("access");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("Should have correct token type in access token")
    void shouldHaveCorrectTokenTypeInAccessToken() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);

        // When
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Then
        assertThat(claims.get("type")).isEqualTo("access");
    }

    @Test
    @DisplayName("Should have correct token type in refresh token")
    void shouldHaveCorrectTokenTypeInRefreshToken() {
        // Given
        String token = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        // When
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Then
        assertThat(claims.get("type")).isEqualTo("refresh");
    }

    @Test
    @DisplayName("Should generate different tokens for same username")
    void shouldGenerateDifferentTokensForSameUsername() throws InterruptedException {
        // Given
        String token1 = jwtTokenProvider.generateAccessToken(TEST_USERNAME);
        
        // Wait enough time to ensure different issued-at time (at least 1 second)
        Thread.sleep(1100);
        
        String token2 = jwtTokenProvider.generateAccessToken(TEST_USERNAME);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Should generate different access and refresh tokens")
    void shouldGenerateDifferentAccessAndRefreshTokens() {
        // When
        String accessToken = jwtTokenProvider.generateAccessToken(TEST_USERNAME);
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        // Then
        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Should return correct access token expiration")
    void shouldReturnCorrectAccessTokenExpiration() {
        // When
        long expiration = jwtTokenProvider.getAccessTokenExpiration();

        // Then
        assertThat(expiration).isEqualTo(ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("Should return correct refresh token expiration")
    void shouldReturnCorrectRefreshTokenExpiration() {
        // When
        long expiration = jwtTokenProvider.getRefreshTokenExpiration();

        // Then
        assertThat(expiration).isEqualTo(REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("Should handle special characters in username")
    void shouldHandleSpecialCharactersInUsername() {
        // Given
        String specialUsername = "user@example.com";

        // When
        String token = jwtTokenProvider.generateAccessToken(specialUsername);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(specialUsername);
    }

    @Test
    @DisplayName("Should handle long username")
    void shouldHandleLongUsername() {
        // Given
        String longUsername = "a".repeat(100);

        // When
        String token = jwtTokenProvider.generateAccessToken(longUsername);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(longUsername);
    }

    @Test
    @DisplayName("Should throw exception when extracting username from invalid token")
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When/Then
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception when extracting claims from invalid token")
    void shouldThrowExceptionWhenExtractingClaimsFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When/Then
        assertThatThrownBy(() -> jwtTokenProvider.getClaimsFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate token with correct issuer")
    void shouldValidateTokenWithCorrectIssuer() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Then
        assertThat(claims.getIssuer()).isEqualTo(TEST_ISSUER);
    }

    @Test
    @DisplayName("Should validate token with correct audience")
    void shouldValidateTokenWithCorrectAudience() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Then
        assertThat(claims.getAudience()).contains(TEST_AUDIENCE);
    }

    @Test
    @DisplayName("Should have issued-at time before expiration time")
    void shouldHaveIssuedAtTimeBeforeExpirationTime() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Then
        assertThat(claims.getIssuedAt()).isBefore(claims.getExpiration());
    }

    @Test
    @DisplayName("Should generate token with expiration approximately equal to configured time")
    void shouldGenerateTokenWithCorrectExpiration() {
        // Given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME);
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // When
        long actualExpiration = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        // Then - Allow 1 second tolerance for processing time
        assertThat(actualExpiration).isBetween(
                ACCESS_TOKEN_EXPIRATION - 1000,
                ACCESS_TOKEN_EXPIRATION + 1000
        );
    }
}
