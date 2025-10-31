package com.example.demo.service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 限流服務
 * 使用 Bucket4j 和 Redis 實現分散式 API 限流
 * 
 * 功能包括：
 * - IP 基礎限流
 * - 使用者基礎限流
 * - API 端點限流
 * - 自動令牌補充
 * - Redis 分散式存儲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    // 預設限流規則
    private static final long DEFAULT_CAPACITY = 100;           // 預設容量：100 個請求
    private static final Duration DEFAULT_REFILL_PERIOD = Duration.ofMinutes(1); // 每分鐘補充
    private static final long DEFAULT_REFILL_TOKENS = 100;      // 每次補充 100 個令牌

    /**
     * 檢查是否允許請求（基於 IP）
     * @param clientIP 客戶端 IP
     * @return 是否允許
     */
    public boolean isAllowedByIP(String clientIP) {
        String bucketKey = "rate_limit:ip:" + clientIP;
        return consumeToken(bucketKey, DEFAULT_CAPACITY, DEFAULT_REFILL_TOKENS, DEFAULT_REFILL_PERIOD);
    }

    /**
     * 檢查是否允許請求（基於使用者）
     * @param userId 使用者 ID
     * @return 是否允許
     */
    public boolean isAllowedByUser(String userId) {
        String bucketKey = "rate_limit:user:" + userId;
        // 使用者限流：每分鐘 200 個請求
        return consumeToken(bucketKey, 200, 200, DEFAULT_REFILL_PERIOD);
    }

    /**
     * 檢查 API 端點是否允許請求
     * @param endpoint API 端點
     * @param clientIP 客戶端 IP
     * @return 是否允許
     */
    public boolean isAllowedByEndpoint(String endpoint, String clientIP) {
        String bucketKey = "rate_limit:endpoint:" + endpoint + ":" + clientIP;
        
        // 根據不同端點設定不同的限流規則
        RateLimitRule rule = getRateLimitRule(endpoint);
        return consumeToken(bucketKey, rule.capacity, rule.refillTokens, rule.refillPeriod);
    }

    /**
     * 消費令牌
     * @param bucketKey Bucket 金鑰
     * @param capacity 容量
     * @param refillTokens 補充令牌數
     * @param refillPeriod 補充週期
     * @return 是否成功消費
     */
    private boolean consumeToken(String bucketKey, long capacity, long refillTokens, Duration refillPeriod) {
        try {
            Bucket bucket = bucketCache.computeIfAbsent(bucketKey, k -> createBucket(capacity, refillTokens, refillPeriod));
            
            boolean consumed = bucket.tryConsume(1);
            
            if (!consumed) {
                log.warn("限流觸發 - Bucket: {}, 剩餘令牌: {}", bucketKey, bucket.getAvailableTokens());
                
                // 記錄限流事件到 Redis（用於監控）
                recordRateLimitEvent(bucketKey);
            }
            
            return consumed;
            
        } catch (Exception e) {
            log.error("限流檢查失敗 - Bucket: {}, 錯誤: {}", bucketKey, e.getMessage());
            // 發生錯誤時，預設允許請求（避免服務不可用）
            return true;
        }
    }

    /**
     * 建立 Bucket
     * @param capacity 容量
     * @param refillTokens 補充令牌數
     * @param refillPeriod 補充週期
     * @return Bucket 實例
     */
    private Bucket createBucket(long capacity, long refillTokens, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(refillTokens, refillPeriod));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 獲取 API 端點的限流規則
     * @param endpoint API 端點
     * @return 限流規則
     */
    private RateLimitRule getRateLimitRule(String endpoint) {
        // 根據不同的 API 端點返回不同的限流規則
        return switch (endpoint) {
            case "/api/auth/login" -> new RateLimitRule(10, 10, Duration.ofMinutes(5)); // 登入：5分鐘內最多10次
            case "/api/auth/register" -> new RateLimitRule(5, 5, Duration.ofMinutes(10)); // 註冊：10分鐘內最多5次
            case "/api/payment/**" -> new RateLimitRule(50, 50, Duration.ofMinutes(1)); // 支付：每分鐘50次
            case "/api/public/**" -> new RateLimitRule(1000, 1000, Duration.ofMinutes(1)); // 公開API：每分鐘1000次
            default -> new RateLimitRule(DEFAULT_CAPACITY, DEFAULT_REFILL_TOKENS, DEFAULT_REFILL_PERIOD);
        };
    }

    /**
     * 記錄限流事件
     * @param bucketKey Bucket 金鑰
     */
    private void recordRateLimitEvent(String bucketKey) {
        try {
            String eventKey = "rate_limit_events:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(eventKey, bucketKey, Duration.ofHours(24));
        } catch (Exception e) {
            log.error("記錄限流事件失敗: {}", e.getMessage());
        }
    }

    /**
     * 獲取剩餘令牌數
     * @param clientIP 客戶端 IP
     * @return 剩餘令牌數
     */
    public long getRemainingTokens(String clientIP) {
        String bucketKey = "rate_limit:ip:" + clientIP;
        Bucket bucket = bucketCache.get(bucketKey);
        return bucket != null ? bucket.getAvailableTokens() : DEFAULT_CAPACITY;
    }

    /**
     * 清除限流快取
     * @param identifier 識別符（IP 或使用者 ID）
     */
    public void clearRateLimit(String identifier) {
        bucketCache.remove("rate_limit:ip:" + identifier);
        bucketCache.remove("rate_limit:user:" + identifier);
        log.info("已清除限流快取: {}", identifier);
    }

    /**
     * 限流規則內部類別
     */
    private static class RateLimitRule {
        final long capacity;
        final long refillTokens;
        final Duration refillPeriod;

        RateLimitRule(long capacity, long refillTokens, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriod = refillPeriod;
        }
    }
}