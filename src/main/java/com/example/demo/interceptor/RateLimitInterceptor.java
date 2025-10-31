package com.example.demo.interceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 限流攔截器
 * 在請求進入控制器之前進行限流檢查
 * 
 * 功能包括：
 * - IP 基礎限流檢查
 * - 使用者基礎限流檢查
 * - API 端點限流檢查
 * - 限流響應處理
 * - 安全事件記錄
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 獲取客戶端 IP
        String clientIP = getClientIP(request);
        String requestURI = request.getRequestURI();
        
        // 跳過某些路徑的限流檢查
        if (shouldSkipRateLimit(requestURI)) {
            return true;
        }

        // 1. IP 基礎限流檢查
        if (!rateLimitService.isAllowedByIP(clientIP)) {
            log.warn("IP 限流觸發 - IP: {}, URI: {}", clientIP, requestURI);
            handleRateLimitExceeded(response, "IP 請求頻率過高，請稍後再試");
            return false;
        }

        // 2. 使用者基礎限流檢查（如果已登入）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            String username = authentication.getName();
            if (!rateLimitService.isAllowedByUser(username)) {
                log.warn("使用者限流觸發 - User: {}, IP: {}, URI: {}", username, clientIP, requestURI);
                handleRateLimitExceeded(response, "使用者請求頻率過高，請稍後再試");
                return false;
            }
        }

        // 3. API 端點限流檢查
        if (!rateLimitService.isAllowedByEndpoint(requestURI, clientIP)) {
            log.warn("API 端點限流觸發 - Endpoint: {}, IP: {}", requestURI, clientIP);
            handleRateLimitExceeded(response, "API 請求頻率過高，請稍後再試");
            return false;
        }

        // 在響應標頭中添加剩餘配額資訊
        long remainingTokens = rateLimitService.getRemainingTokens(clientIP);
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));
        response.setHeader("X-Rate-Limit-Limit", "100");

        return true;
    }

    /**
     * 處理限流超出情況
     * @param response HTTP 響應
     * @param message 錯誤訊息
     * @throws IOException IO 異常
     */
    private void handleRateLimitExceeded(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 設定限流相關的響應標頭
        response.setHeader("Retry-After", "60"); // 建議 60 秒後重試
        response.setHeader("X-Rate-Limit-Remaining", "0");
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(System.currentTimeMillis() + 60000));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", message);
        errorResponse.put("code", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("retryAfter", 60);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 獲取客戶端真實 IP 地址
     * @param request HTTP 請求
     * @return 客戶端 IP
     */
    private String getClientIP(HttpServletRequest request) {
        // 檢查各種可能的 IP 標頭
        String[] ipHeaders = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };

        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多個 IP，取第一個
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 判斷是否應該跳過限流檢查
     * @param requestURI 請求 URI
     * @return 是否跳過
     */
    private boolean shouldSkipRateLimit(String requestURI) {
        // 跳過健康檢查和靜態資源的限流
        return requestURI.equals("/actuator/health") ||
               requestURI.equals("/actuator/info") ||
               requestURI.startsWith("/static/") ||
               requestURI.startsWith("/css/") ||
               requestURI.startsWith("/js/") ||
               requestURI.startsWith("/images/") ||
               requestURI.equals("/favicon.ico") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/v3/api-docs/");
    }
}