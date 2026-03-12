package com.kiro.metadata.annotation;

import com.kiro.metadata.entity.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 用于方法级别的权限控制
 * 
 * 使用示例：
 * @RequireRole({UserRole.ADMIN, UserRole.DEVELOPER})
 * public void updateTable() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 允许访问的角色列表
     * 用户只需拥有其中一个角色即可访问
     */
    UserRole[] value();
}
