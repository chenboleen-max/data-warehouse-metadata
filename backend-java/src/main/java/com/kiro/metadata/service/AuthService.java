package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.LoginRequest;
import com.kiro.metadata.dto.response.TokenResponse;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.entity.UserRole;
import com.kiro.metadata.exception.UnauthorizedException;
import com.kiro.metadata.repository.UserRepository;
import com.kiro.metadata.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 * 处理用户登录、登出、Token 刷新等认证相关操作
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return Token 响应
     */
    @Transactional
    public TokenResponse login(String username, String password) {
        log.info("User login attempt: {}", username);
        
        // 查询用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("用户名或密码错误"));
        
        // 检查用户是否被禁用
        if (!user.getIsActive()) {
            throw new UnauthorizedException("用户账号已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // 转换为秒
                .build();
    }
    
    /**
     * 用户登出
     * 将 Token 加入黑名单
     * 
     * @param username 用户名
     */
    public void logout(String username) {
        log.info("User logged out: {}", username);
        // 简化实现：仅记录日志
        // 实际应该将当前用户的所有 token 加入黑名单
    }
    
    /**
     * 刷新 Token
     * 
     * @param refreshToken Refresh Token
     * @return 新的 Token 响应
     */
    public TokenResponse refreshToken(String refreshToken) {
        // 验证 Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("无效的 Refresh Token");
        }
        
        // 检查是否为 Refresh Token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Token 类型错误");
        }
        
        // 检查 Token 是否在黑名单中
        String key = TOKEN_BLACKLIST_PREFIX + refreshToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new UnauthorizedException("Token 已失效");
        }
        
        // 获取用户名
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // 查询用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("用户不存在"));
        
        // 检查用户是否被禁用
        if (!user.getIsActive()) {
            throw new UnauthorizedException("用户账号已被禁用");
        }
        
        // 生成新的 Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        // 将旧的 Refresh Token 加入黑名单
        Claims claims = jwtTokenProvider.getClaimsFromToken(refreshToken);
        long expirationTime = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        long ttl = expirationTime - currentTime;
        
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
        }
        
        log.info("Token refreshed for user: {}", username);
        
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }
    
    /**
     * 获取当前登录用户（从 SecurityContext）
     * 
     * @return 当前用户
     */
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("用户不存在"));
    }
    
    /**
     * 根据用户名获取用户
     * 
     * @param username 用户名
     * @return 用户
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("用户不存在"));
    }
    
    /**
     * 检查用户权限
     * 
     * @param user 用户
     * @param operation 操作类型
     * @return 是否有权限
     */
    public boolean checkPermission(User user, String operation) {
        UserRole role = user.getRole();
        
        return switch (operation) {
            case "read" -> true; // 所有角色都可以读取
            case "update" -> role == UserRole.DEVELOPER || role == UserRole.ADMIN;
            case "delete" -> role == UserRole.ADMIN;
            case "create_catalog" -> role == UserRole.ADMIN;
            default -> false;
        };
    }
}
