package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 異常偵測服務
 * 使用簡單的規則引擎進行安全事件偵測
 * （暫時不使用 Drools，避免複雜性）
 * 
 * 功能包括：
 * - 登入失敗次數監控
 * - 異常 IP 行為偵測  
 * - 可疑請求模式分析
 * - 自動告警機制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    // 儲存各種異常計數器
    private final Map<String, AtomicInteger> loginFailureCounter = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> ipRequestCounter = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastLoginFailureTime = new ConcurrentHashMap<>();
    
    // 閾值設定
    private static final int MAX_LOGIN_FAILURES = 5;       // 最大登入失敗次數
    private static final int MAX_REQUESTS_PER_MINUTE = 300; // 每分鐘最大請求數
    private static final int SUSPICIOUS_PATTERN_THRESHOLD = 10; // 可疑模式閾值

    /**
     * 記錄登入失敗事件
     * @param clientIP 客戶端 IP
     * @param username 使用者名稱
     */
    public void recordLoginFailure(String clientIP, String username) {
        String key = clientIP + ":" + username;
        
        AtomicInteger counter = loginFailureCounter.computeIfAbsent(key, k -> new AtomicInteger(0));
        int failures = counter.incrementAndGet();
        lastLoginFailureTime.put(key, LocalDateTime.now());
        
        log.warn("登入失敗事件 - IP: {}, 使用者: {}, 失敗次數: {}", clientIP, username, failures);
        
        if (failures >= MAX_LOGIN_FAILURES) {
            triggerSecurityAlert("MULTIPLE_LOGIN_FAILURES", 
                String.format("IP %s 對使用者 %s 的登入失敗次數達到 %d 次", clientIP, username, failures));
        }
    }

    /**
     * 記錄可疑 IP 活動
     * @param clientIP 客戶端 IP
     * @param endpoint 請求端點
     */
    public void recordSuspiciousIPActivity(String clientIP, String endpoint) {
        AtomicInteger counter = ipRequestCounter.computeIfAbsent(clientIP, k -> new AtomicInteger(0));
        int requests = counter.incrementAndGet();
        
        if (requests > MAX_REQUESTS_PER_MINUTE) {
            triggerSecurityAlert("EXCESSIVE_REQUESTS", 
                String.format("IP %s 在短時間內發出了 %d 個請求，端點: %s", clientIP, requests, endpoint));
        }
    }

    /**
     * 檢測異常使用者行為
     * @param username 使用者名稱
     * @param action 用戶操作
     * @param clientIP 客戶端 IP
     */
    public void detectAnomalousUserBehavior(String username, String action, String clientIP) {
        // 檢測深夜活動（簡單示例）
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        if (hour >= 2 && hour <= 5) { // 凌晨 2-5 點
            log.warn("深夜活動偵測 - 使用者: {}, 動作: {}, IP: {}, 時間: {}", 
                username, action, clientIP, now);
            
            if (isHighRiskAction(action)) {
                triggerSecurityAlert("LATE_NIGHT_HIGH_RISK_ACTIVITY", 
                    String.format("使用者 %s 在深夜執行高風險操作: %s, IP: %s", username, action, clientIP));
            }
        }
        
        // 檢測地理位置異常（簡化版本 - 基於 IP 變化）
        detectGeographicalAnomaly(username, clientIP);
    }

    /**
     * 檢測地理位置異常
     * @param username 使用者名稱  
     * @param currentIP 當前 IP
     */
    private void detectGeographicalAnomaly(String username, String currentIP) {
        // 這是一個簡化的地理檢測，實際應該使用 GeoIP 服務
        String lastKnownIPKey = "last_ip:" + username;
        String lastIP = getLastKnownIP(lastKnownIPKey);
        
        if (lastIP != null && !lastIP.equals(currentIP)) {
            // IP 發生變化，可能是地理位置異常
            if (isSignificantIPChange(lastIP, currentIP)) {
                triggerSecurityAlert("GEOGRAPHICAL_ANOMALY", 
                    String.format("使用者 %s 的 IP 位置發生顯著變化: %s -> %s", username, lastIP, currentIP));
            }
        }
        
        // 更新最後已知 IP
        updateLastKnownIP(lastKnownIPKey, currentIP);
    }

    /**
     * 觸發安全告警
     * @param alertType 告警類型
     * @param message 告警訊息
     */
    private void triggerSecurityAlert(String alertType, String message) {
        log.error("🚨 安全告警 [{}]: {}", alertType, message);
        
        // 這裡可以整合：
        // 1. 發送郵件通知
        // 2. Slack/Teams 通知
        // 3. 寫入安全事件資料庫
        // 4. 觸發自動封鎖機制
        
        // 示例：記錄到系統日誌
        recordSecurityEvent(alertType, message);
    }

    /**
     * 記錄安全事件
     * @param eventType 事件類型
     * @param message 事件訊息
     */
    private void recordSecurityEvent(String eventType, String message) {
        // TODO: 實作寫入資料庫或外部系統的邏輯
        log.info("安全事件已記錄 - 類型: {}, 訊息: {}, 時間: {}", 
            eventType, message, LocalDateTime.now());
    }

    /**
     * 判斷是否為高風險操作
     * @param action 操作類型
     * @return 是否為高風險
     */
    private boolean isHighRiskAction(String action) {
        return action.contains("delete") || 
               action.contains("transfer") || 
               action.contains("withdraw") ||
               action.contains("admin") ||
               action.contains("config");
    }

    /**
     * 判斷 IP 變化是否顯著
     * @param oldIP 舊 IP
     * @param newIP 新 IP  
     * @return 是否為顯著變化
     */
    private boolean isSignificantIPChange(String oldIP, String newIP) {
        // 簡化的 IP 比較邏輯
        // 實際應該使用 GeoIP 服務比較地理位置
        String[] oldParts = oldIP.split("\\.");
        String[] newParts = newIP.split("\\.");
        
        if (oldParts.length != 4 || newParts.length != 4) {
            return true; // 無效 IP 格式
        }
        
        // 如果前兩段不同，認為是顯著變化
        return !oldParts[0].equals(newParts[0]) || !oldParts[1].equals(newParts[1]);
    }

    /**
     * 獲取最後已知 IP
     * @param key 鍵值
     * @return 最後已知 IP
     */
    private String getLastKnownIP(String key) {
        // TODO: 從 Redis 或資料庫獲取
        return null;
    }

    /**
     * 更新最後已知 IP
     * @param key 鍵值
     * @param ip IP 地址
     */
    private void updateLastKnownIP(String key, String ip) {
        // TODO: 儲存到 Redis 或資料庫
    }

    /**
     * 清理過期的計數器
     */
    public void cleanupExpiredCounters() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        lastLoginFailureTime.entrySet().removeIf(entry -> 
            entry.getValue().isBefore(oneHourAgo));
        
        log.info("已清理過期的異常偵測計數器");
    }

    /**
     * 重置使用者的失敗計數（成功登入後調用）
     * @param clientIP 客戶端 IP
     * @param username 使用者名稱
     */
    public void resetLoginFailureCount(String clientIP, String username) {
        String key = clientIP + ":" + username;
        loginFailureCounter.remove(key);
        lastLoginFailureTime.remove(key);
        log.debug("已重置登入失敗計數 - IP: {}, 使用者: {}", clientIP, username);
    }
}