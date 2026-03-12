package com.kiro.metadata.aspect;

import com.kiro.metadata.annotation.RequireRole;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.entity.UserRole;
import com.kiro.metadata.exception.ForbiddenException;
import com.kiro.metadata.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 权限验证切面
 * 拦截带有 @RequireRole 注解的方法，验证用户权限
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {
    
    private final AuthService authService;
    
    /**
     * 环绕通知：验证用户权限
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(com.kiro.metadata.annotation.RequireRole)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取 @RequireRole 注解
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        if (requireRole == null) {
            return joinPoint.proceed();
        }
        
        // 获取当前用户
        User currentUser;
        try {
            currentUser = authService.getCurrentUser();
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            throw new ForbiddenException("未登录或登录已过期");
        }
        
        // 获取允许的角色列表
        UserRole[] allowedRoles = requireRole.value();
        
        // 检查用户角色是否在允许列表中
        boolean hasPermission = Arrays.asList(allowedRoles).contains(currentUser.getRole());
        
        if (!hasPermission) {
            log.warn("User {} with role {} attempted to access method {} which requires roles {}",
                    currentUser.getUsername(),
                    currentUser.getRole(),
                    method.getName(),
                    Arrays.toString(allowedRoles));
            
            throw new ForbiddenException(
                    String.format("权限不足，需要以下角色之一：%s", Arrays.toString(allowedRoles)));
        }
        
        log.debug("User {} with role {} has permission to access method {}",
                currentUser.getUsername(),
                currentUser.getRole(),
                method.getName());
        
        // 执行方法
        return joinPoint.proceed();
    }
}
