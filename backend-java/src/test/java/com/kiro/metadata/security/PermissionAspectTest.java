package com.kiro.metadata.security;

import com.kiro.metadata.entity.User;
import com.kiro.metadata.entity.UserRole;
import com.kiro.metadata.repository.UserRepository;
import com.kiro.metadata.service.AuthService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PermissionAspect
 * 
 * Tests AOP-based permission verification for methods annotated with @RequireRole
 * 
 * Validates: Requirements 6.1, 6.3, 6.4, 6.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionAspect Tests")
class PermissionAspectTest {

    @InjectMocks
    private PermissionAspect permissionAspect;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User adminUser;
    private User developerUser;
    private User guestUser;

    @BeforeEach
    void setUp() {
        // Create test users
        adminUser = createUser("admin", UserRole.ADMIN);
        developerUser = createUser("developer", UserRole.DEVELOPER);
        guestUser = createUser("guest", UserRole.GUEST);

        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);
    }

    private User createUser(String username, UserRole role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("$2a$10$hashedPassword");
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private void setupAuthentication(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
    }

    private void setupMethodSignature(String methodName) throws NoSuchMethodException {
        Method method = TestController.class.getMethod(methodName);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
    }

    // ========== Successful Permission Checks ==========

    @Test
    @DisplayName("ADMIN can access method requiring ADMIN role")
    void testAdminCanAccessAdminMethod() throws Throwable {
        setupAuthentication("admin");
        setupMethodSignature("adminOnlyMethod");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(authService.checkPermission(any(User.class), any(), eq("delete"))).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("success");

        RequireRole annotation = TestController.class.getMethod("adminOnlyMethod").getAnnotation(RequireRole.class);
        Object result = permissionAspect.checkPermission(joinPoint, annotation);

        assertThat(result).isEqualTo("success");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("DEVELOPER can access method requiring DEVELOPER or ADMIN role")
    void testDeveloperCanAccessDeveloperMethod() throws Throwable {
        setupAuthentication("developer");
        setupMethodSignature("developerMethod");
        when(userRepository.findByUsername("developer")).thenReturn(Optional.of(developerUser));
        when(authService.checkPermission(any(User.class), any(), eq("update"))).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("success");

        RequireRole annotation = TestController.class.getMethod("developerMethod").getAnnotation(RequireRole.class);
        Object result = permissionAspect.checkPermission(joinPoint, annotation);

        assertThat(result).isEqualTo("success");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("GUEST can access method requiring any role with read action")
    void testGuestCanAccessReadMethod() throws Throwable {
        setupAuthentication("guest");
        setupMethodSignature("publicReadMethod");
        when(userRepository.findByUsername("guest")).thenReturn(Optional.of(guestUser));
        when(authService.checkPermission(any(User.class), any(), eq("read"))).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("success");

        RequireRole annotation = TestController.class.getMethod("publicReadMethod").getAnnotation(RequireRole.class);
        Object result = permissionAspect.checkPermission(joinPoint, annotation);

        assertThat(result).isEqualTo("success");
        verify(joinPoint).proceed();
    }

    // ========== Access Denied Tests ==========

    @Test
    @DisplayName("GUEST cannot access method requiring update action")
    void testGuestCannotAccessUpdateMethod() throws Throwable {
        setupAuthentication("guest");
        setupMethodSignature("developerMethod");
        when(userRepository.findByUsername("guest")).thenReturn(Optional.of(guestUser));
        when(authService.checkPermission(any(User.class), any(), eq("update"))).thenReturn(false);

        RequireRole annotation = TestController.class.getMethod("developerMethod").getAnnotation(RequireRole.class);

        assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("You do not have permission to perform action 'update'");

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("DEVELOPER cannot access ADMIN-only method")
    void testDeveloperCannotAccessAdminMethod() throws Throwable {
        setupAuthentication("developer");
        setupMethodSignature("adminOnlyMethod");
        when(userRepository.findByUsername("developer")).thenReturn(Optional.of(developerUser));

        RequireRole annotation = TestController.class.getMethod("adminOnlyMethod").getAnnotation(RequireRole.class);

        assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Role DEVELOPER is not allowed to access this resource");

        verify(joinPoint, never()).proceed();
    }

    // ========== Authentication Tests ==========

    @Test
    @DisplayName("Throws AccessDeniedException when no authentication in SecurityContext")
    void testNoAuthentication() throws Throwable {
        when(securityContext.getAuthentication()).thenReturn(null);
        setupMethodSignature("publicReadMethod");

        RequireRole annotation = TestController.class.getMethod("publicReadMethod").getAnnotation(RequireRole.class);

        assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Authentication required");

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Throws AccessDeniedException when user is not authenticated")
    void testNotAuthenticated() throws Throwable {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        setupMethodSignature("publicReadMethod");

        RequireRole annotation = TestController.class.getMethod("publicReadMethod").getAnnotation(RequireRole.class);

        assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Authentication required");

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Throws UsernameNotFoundException when user not found in database")
    void testUserNotFoundInDatabase() throws Throwable {
        setupAuthentication("nonexistent");
        setupMethodSignature("publicReadMethod");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RequireRole annotation = TestController.class.getMethod("publicReadMethod").getAnnotation(RequireRole.class);

        assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found: nonexistent");

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Throws AccessDeniedException when user account is disabled")
    void testDisabledAccount() throws Throwable {
        setupAuthentication("guest");
        setupMethodSignature("publicReadMethod");
        
        User disabledUser = createUser("guest", UserRole.GUEST);
        disabledUser.setIsActive(false);
        when(userRepository.findByUsername("guest")).thenReturn(Optional.of(disabledUser));

        RequireRole annotation = TestController.class.getMethod("publicReadMethod").getAnnotation(RequireRole.class);

        assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Account is disabled");

        verify(joinPoint, never()).proceed();
    }

    // ========== Test Controller with @RequireRole annotations ==========

    /**
     * Test controller class with various @RequireRole annotations
     * Used for testing the aspect behavior
     */
    static class TestController {

        @RequireRole(roles = {UserRole.ADMIN}, action = "delete")
        public String adminOnlyMethod() {
            return "admin";
        }

        @RequireRole(roles = {UserRole.ADMIN, UserRole.DEVELOPER}, action = "update")
        public String developerMethod() {
            return "developer";
        }

        @RequireRole(roles = {UserRole.ADMIN, UserRole.DEVELOPER, UserRole.GUEST}, action = "read")
        public String publicReadMethod() {
            return "public";
        }
    }
}
