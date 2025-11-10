package com.example.demo.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 服務類別
 * 負責 JWT Token 的生成、驗證和解析
 * 
 * 功能包括：
 * - 生成 Access Token 和 Refresh Token
 * - 驗證 Token 的有效性和完整性
 * - 解析 Token 中的使用者資訊
 * - Token 過期時間管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    /**
     * 步驟說明：
     * 1. 注入 JWT 配置屬性
     * 2. 使用配置類別替代 @Value 註解
     * 3. 提供更好的配置管理和驗證
     */
    private final JwtProperties jwtProperties;

    /**
     * 生成 JWT Token
     * @param userDetails 使用者詳細資訊
     * @return JWT Token 字符串
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * 生成包含額外聲明的 JWT Token
     * @param extraClaims 額外的聲明資訊
     * @param userDetails 使用者詳細資訊
     * @return JWT Token 字符串
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtProperties.getExpiration());
    }

    /**
     * 生成 Refresh Token
     * @param userDetails 使用者詳細資訊
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshExpiration());
    }

    /**
     * 建立 Token
     * @param extraClaims 額外聲明
     * @param userDetails 使用者詳細資訊
     * @param expiration 過期時間
     * @return JWT Token 字符串
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * 從 Token 中提取使用者名稱
     * @param token JWT Token
     * @return 使用者名稱
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 從 Token 中提取過期時間
     * @param token JWT Token
     * @return 過期時間
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 從 Token 中提取特定聲明
     * @param token JWT Token
     * @param claimsResolver 聲明解析器
     * @return 聲明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 驗證 Token 是否有效
     * @param token JWT Token
     * @param userDetails 使用者詳細資訊
     * @return 是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token 驗證失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 檢查 Token 是否過期
     * @param token JWT Token
     * @return 是否過期
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 提取 Token 中的所有聲明
     * @param token JWT Token
     * @return 聲明集合
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 獲取簽名金鑰
     * @return 簽名金鑰
     */
    /**
     * 獲取簽名金鑰
     * 步驟說明：
     * 1. 從配置類別獲取標準化密鑰
     * 2. 確保密鑰長度符合 HMAC-SHA256 要求
     * 3. 生成 SecretKey 物件
     */
    private SecretKey getSignInKey() {
        String secret = jwtProperties.getStandardizedSecret();
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 檢查 Token 是否即將過期（15分鐘內）
     * @param token JWT Token
     * @return 是否即將過期
     */
    public boolean isTokenAboutToExpire(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        long timeUntilExpiry = expiration.getTime() - now.getTime();
        return timeUntilExpiry < 900000; // 15 分鐘 = 900000 毫秒
    }

    /**
     * 從 Token 中提取使用者 ID（如果有的話）
     * @param token JWT Token
     * @return 使用者 ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            return userId != null ? Long.valueOf(userId.toString()) : null;
        });
    }

    /**
     * 從 Token 中提取使用者角色
     * @param token JWT Token
     * @return 使用者角色
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (java.util.List<String>) claims.get("roles"));
    }

    /**
     * 獲取 JWT ID (JTI)
     * @param token JWT Token
     * @return JWT ID
     */
    public String getJwtId(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * 獲取 Token 過期時間戳
     * @param token JWT Token
     * @return 過期時間戳（毫秒）
     */
    public long getExpirationTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime();
    }

    /**
     * 生成帶有 JTI 的 Token
     * @param extraClaims 額外的聲明
     * @param userDetails 使用者詳細資訊
     * @param jwtId JWT ID
     * @return JWT Token
     */
    public String generateTokenWithJti(Map<String, Object> extraClaims, UserDetails userDetails, String jwtId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .id(jwtId) // 設置 JTI
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();
    }
}