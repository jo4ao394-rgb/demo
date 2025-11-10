package com.example.demo.security;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * JWT + Redis Token 管理服務
 * 
 * 功能：
 * 1. JWT Token 黑名單管理
 * 2. Token 刷新機制
 * 3. 多設備登入控制
 * 4. Token 撤銷功能
 */
@Slf4j
@Service
public class JwtRedisTokenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtService jwtService;

    // Redis Key 前綴
    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String USER_TOKENS_PREFIX = "jwt:user:";
    private static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";

    /**
     * 將 Token 加入黑名單
     */
    public void blacklistToken(String token) {
        try {
            String jti = jwtService.getJwtId(token);
            long expiration = jwtService.getExpirationTime(token);
            long currentTime = System.currentTimeMillis();
            
            if (expiration > currentTime) {
                long ttl = expiration - currentTime;
                String key = TOKEN_BLACKLIST_PREFIX + jti;
                redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
                log.info("Token {} 已加入黑名單，TTL: {} ms", jti, ttl);
            }
        } catch (Exception e) {
            log.error("將 Token 加入黑名單時發生錯誤", e);
        }
    }

    /**
     * 檢查 Token 是否在黑名單中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String jti = jwtService.getJwtId(token);
            String key = TOKEN_BLACKLIST_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("檢查 Token 黑名單狀態時發生錯誤", e);
            return true; // 安全起見，出錯時視為已黑名單
        }
    }

    /**
     * 儲存用戶的 Active Tokens
     */
    public void storeUserToken(String username, String jti, long expiration) {
        try {
            String key = USER_TOKENS_PREFIX + username;
            long ttl = expiration - System.currentTimeMillis();
            
            // 使用 Set 結構儲存用戶的多個 token
            redisTemplate.opsForSet().add(key, jti);
            redisTemplate.expire(key, Duration.ofMillis(ttl));
            
            log.debug("為用戶 {} 儲存 Token: {}", username, jti);
        } catch (Exception e) {
            log.error("儲存用戶 Token 時發生錯誤", e);
        }
    }

    /**
     * 移除用戶的 Token
     */
    public void removeUserToken(String username, String jti) {
        try {
            String key = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().remove(key, jti);
            log.debug("為用戶 {} 移除 Token: {}", username, jti);
        } catch (Exception e) {
            log.error("移除用戶 Token 時發生錯誤", e);
        }
    }

    /**
     * 撤銷用戶的所有 Token（登出所有設備）
     */
    public void revokeAllUserTokens(String username) {
        try {
            String key = USER_TOKENS_PREFIX + username;
            var tokens = redisTemplate.opsForSet().members(key);
            
            if (tokens != null) {
                // 將所有 token 加入黑名單
                for (String jti : tokens) {
                    String blacklistKey = TOKEN_BLACKLIST_PREFIX + jti;
                    redisTemplate.opsForValue().set(blacklistKey, "revoked", 24, TimeUnit.HOURS);
                }
                
                // 清空用戶 token 集合
                redisTemplate.delete(key);
                
                log.info("已撤銷用戶 {} 的所有 Token，共 {} 個", username, tokens.size());
            }
        } catch (Exception e) {
            log.error("撤銷用戶所有 Token 時發生錯誤", e);
        }
    }

    /**
     * 儲存 Refresh Token
     */
    public void storeRefreshToken(String refreshToken, String username, long expiration) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshToken;
            long ttl = expiration - System.currentTimeMillis();
            
            redisTemplate.opsForValue().set(key, username, ttl, TimeUnit.MILLISECONDS);
            log.debug("為用戶 {} 儲存 Refresh Token", username);
        } catch (Exception e) {
            log.error("儲存 Refresh Token 時發生錯誤", e);
        }
    }

    /**
     * 驗證並獲取 Refresh Token 對應的用戶名
     */
    public String validateRefreshToken(String refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshToken;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("驗證 Refresh Token 時發生錯誤", e);
            return null;
        }
    }

    /**
     * 移除 Refresh Token
     */
    public void removeRefreshToken(String refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshToken;
            redisTemplate.delete(key);
            log.debug("已移除 Refresh Token");
        } catch (Exception e) {
            log.error("移除 Refresh Token 時發生錯誤", e);
        }
    }

    /**
     * 獲取用戶的活躍 Token 數量
     */
    public Long getUserActiveTokenCount(String username) {
        try {
            String key = USER_TOKENS_PREFIX + username;
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("獲取用戶活躍 Token 數量時發生錯誤", e);
            return 0L;
        }
    }

    /**
     * 清理過期的黑名單 Token（可選，Redis 會自動清理）
     */
    public void cleanupExpiredTokens() {
        // Redis TTL 會自動清理，這個方法主要用於監控和日誌
        log.info("Token 清理任務執行完成");
    }
}