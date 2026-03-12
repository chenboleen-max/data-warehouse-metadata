package com.kiro.metadata.security;

import com.kiro.metadata.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 * 负责生成、验证和解析 JWT Token
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;
    
    @Value("${jwt.issuer:kiro-metadata-system}")
    private String issuer;
    
    /**
     * 生成 Access Token
     * 
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token 字符串
     */
    public String generateAccessToken(String username, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.name())
                .claim("type", "access")
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 生成 Refresh Token
     * 
     * @param username 用户名
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
    
    /**
     * 从 Token 中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }
    
    /**
     * 从 Token 中获取所有声明
     * 
     * @param token JWT Token
     * @return Claims 对象
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 从 Token 中获取用户角色
     * 
     * @param token JWT Token
     * @return 用户角色
     */
    public UserRole getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String role = claims.get("role", String.class);
        return role != null ? UserRole.valueOf(role) : null;
    }
    
    /**
     * 检查 Token 是否为 Access Token
     * 
     * @param token JWT Token
     * @return 是否为 Access Token
     */
    public boolean isAccessToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String type = claims.get("type", String.class);
        return "access".equals(type);
    }
    
    /**
     * 检查 Token 是否为 Refresh Token
     * 
     * @param token JWT Token
     * @return 是否为 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String type = claims.get("type", String.class);
        return "refresh".equals(type);
    }
    
    /**
     * 获取签名密钥
     * 
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
