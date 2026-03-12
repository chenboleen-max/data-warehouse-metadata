package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.LoginRequest;
import com.kiro.metadata.dto.response.TokenResponse;
import com.kiro.metadata.dto.response.UserResponse;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.entity.UserRole;
import com.kiro.metadata.repository.UserRepository;
import com.kiro.metadata.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * 
 * Tests authentication operations including login, logout, token refresh, and current user retrieval
 * 
 * Validates: Requirements 6.2, 14.2 (Authentication and Password Security)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setRole(UserRole.DEVELOPER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup login request
        loginRequest = new LoginRequest("testuser", "password123");

        // Setup Redis template mock
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("testuser")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

        // Act
        TokenResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPasswordHash());
        verify(userRepository).updateById(testUser);
        verify(jwtTokenProvider).generateAccessToken("testuser");
        verify(jwtTokenProvider).generateRefreshToken("testuser");
    }

    @Test
    @DisplayName("Login - User Not Found")
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Account Disabled")
    void login_AccountDisabled() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Account is disabled");

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Invalid Password")
    void login_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPasswordHash());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("Logout - Success")
    void logout_Success() {
        // Arrange
        String token = "valid-token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

        // Act
        boolean result = authService.logout(token);

        // Assert
        assertThat(result).isTrue();
        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(valueOperations).set(
                eq("auth:blacklist:" + token),
                eq("testuser"),
                eq(86400000L),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Logout - Invalid Token")
    void logout_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // Act
        boolean result = authService.logout(token);

        // Assert
        assertThat(result).isFalse();
        verify(jwtTokenProvider).validateToken(token);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Refresh Token - Success")
    void refreshToken_Success() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken("testuser")).thenReturn("new-access-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

        // Act
        TokenResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(jwtTokenProvider).getUsernameFromToken(refreshToken);
        verify(userRepository).findByUsername("testuser");
        verify(jwtTokenProvider).generateAccessToken("testuser");
    }

    @Test
    @DisplayName("Refresh Token - Invalid Token")
    void refreshToken_InvalidToken() {
        // Arrange
        String refreshToken = "invalid-refresh-token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Refresh Token - User Not Found")
    void refreshToken_UserNotFound() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Refresh Token - Account Disabled")
    void refreshToken_AccountDisabled() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        testUser.setIsActive(false);
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Account is disabled");

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Get Current User - Success")
    void getCurrentUser_Success() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", null, null
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = authService.getCurrentUser();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUser.getId());
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.DEVELOPER);
        assertThat(response.getIsActive()).isTrue();

        verify(userRepository).findByUsername("testuser");

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Get Current User - No Authentication")
    void getCurrentUser_NoAuthentication() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("No authenticated user");

        verify(userRepository, never()).findByUsername(anyString());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Get Current User - User Not Found")
    void getCurrentUser_UserNotFound() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", null, null
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByUsername("testuser");

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Is Token Blacklisted - True")
    void isTokenBlacklisted_True() {
        // Arrange
        String token = "blacklisted-token";
        when(stringRedisTemplate.hasKey("auth:blacklist:" + token)).thenReturn(true);

        // Act
        boolean result = authService.isTokenBlacklisted(token);

        // Assert
        assertThat(result).isTrue();
        verify(stringRedisTemplate).hasKey("auth:blacklist:" + token);
    }

    @Test
    @DisplayName("Is Token Blacklisted - False")
    void isTokenBlacklisted_False() {
        // Arrange
        String token = "valid-token";
        when(stringRedisTemplate.hasKey("auth:blacklist:" + token)).thenReturn(false);

        // Act
        boolean result = authService.isTokenBlacklisted(token);

        // Assert
        assertThat(result).isFalse();
        verify(stringRedisTemplate).hasKey("auth:blacklist:" + token);
    }
}
