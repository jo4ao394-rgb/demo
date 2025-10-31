package com.example.demo.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 認證入口點
 * 處理認證失敗的情況，返回統一的錯誤響應
 * 
 * 功能包括：
 * - 攔截未認證的請求
 * - 返回 401 未授權狀態碼
 * - 提供結構化的錯誤資訊
 * - 記錄安全事件日誌
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        log.warn("認證失敗 - IP: {}, URI: {}, 錯誤: {}", 
                getClientIP(request), 
                request.getRequestURI(), 
                authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = createErrorResponse(request, authException);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 建立錯誤響應
     * @param request HTTP 請求
     * @param authException 認證異常
     * @return 錯誤響應 Map
     */
    private Map<String, Object> createErrorResponse(HttpServletRequest request, 
                                                   AuthenticationException authException) {
        Map<String, Object> errorResponse = new HashMap<>();
        
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "認證失敗：請提供有效的認證資訊");
        errorResponse.put("path", request.getRequestURI());
        
        // 根據不同的認證異常提供更具體的錯誤訊息
        String detailMessage = getDetailedErrorMessage(authException, request);
        if (detailMessage != null) {
            errorResponse.put("detail", detailMessage);
        }
        
        return errorResponse;
    }

    /**
     * 根據異常類型提供詳細的錯誤訊息
     * @param authException 認證異常
     * @param request HTTP 請求
     * @return 詳細錯誤訊息
     */
    private String getDetailedErrorMessage(AuthenticationException authException, 
                                         HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null) {
            return "缺少 Authorization 標頭";
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            return "Authorization 標頭格式錯誤，應為 'Bearer {token}'";
        }
        
        // 可以根據具體的異常類型提供更詳細的訊息
        if (authException.getMessage().contains("expired")) {
            return "Token 已過期，請重新登入";
        }
        
        if (authException.getMessage().contains("signature")) {
            return "Token 簽章無效";
        }
        
        return "Token 無效或已失效";
    }

    /**
     * 獲取客戶端真實 IP 地址
     * @param request HTTP 請求
     * @return 客戶端 IP
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}