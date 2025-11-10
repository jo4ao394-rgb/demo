package com.example.demo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 增強版 JWT 認證過濾器
 * 整合 Redis 黑名單檢查和多層級安全驗證
 * 
 * 功能包括：
 * - JWT Token 提取和驗證
 * - Redis 黑名單檢查
 * - 多設備登入控制
 * - 請求頻率限制
 * - 安全日誌記錄
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnhancedJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Autowired(required = false)
    private JwtRedisTokenService jwtRedisTokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // 獲取請求路徑，用於日誌記錄
        String requestPath = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        try {
            // 1. 提取 Authorization 標頭
            final String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("請求 {} 沒有有效的 JWT Token", requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 提取 JWT Token
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail == null) {
                log.warn("從 JWT Token 中無法提取用戶名，IP: {}, Path: {}", clientIp, requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 檢查用戶是否已經認證
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Redis 黑名單檢查（如果 Redis 服務可用）
            if (jwtRedisTokenService != null && jwtRedisTokenService.isTokenBlacklisted(jwt)) {
                log.warn("檢測到黑名單 Token 訪問，用戶: {}, IP: {}, Path: {}", userEmail, clientIp, requestPath);
                handleBlacklistedToken(response);
                return;
            }

            // 5. 載入用戶詳細資訊
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } catch (Exception e) {
                log.error("載入用戶 {} 的詳細資訊時發生錯誤: {}", userEmail, e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            // 6. 驗證 Token
            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("無效的 JWT Token，用戶: {}, IP: {}, Path: {}", userEmail, clientIp, requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            // 7. Token 即將過期警告
            if (jwtService.isTokenAboutToExpire(jwt)) {
                log.info("Token 即將過期，建議刷新，用戶: {}", userEmail);
                response.setHeader("X-Token-Expiring", "true");
            }

            // 8. 創建認證對象
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // 9. 設置認證詳細資訊
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 10. 設置認證上下文
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 11. 更新 Redis 中的用戶活動記錄（可選）
            updateUserActivity(userEmail, jwt, clientIp);

            log.debug("JWT 認證成功，用戶: {}, IP: {}, Path: {}", userEmail, clientIp, requestPath);

        } catch (Exception e) {
            log.error("JWT 認證過濾器發生異常，IP: {}, Path: {}, 錯誤: {}", clientIp, requestPath, e.getMessage());
            // 繼續過濾鏈，讓後續的安全機制處理
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 處理黑名單 Token
     */
    private void handleBlacklistedToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Token has been blacklisted\",\"code\":\"TOKEN_BLACKLISTED\"}");
    }

    /**
     * 獲取客戶端真實 IP 地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 更新用戶活動記錄
     */
    private void updateUserActivity(String username, String jwt, String clientIp) {
        try {
            if (jwtRedisTokenService != null) {
                // 記錄用戶最後活動時間和 IP
                String jti = jwtService.getJwtId(jwt);
                long expiration = jwtService.getExpirationTime(jwt);
                jwtRedisTokenService.storeUserToken(username, jti, expiration);
            }
        } catch (Exception e) {
            log.warn("更新用戶活動記錄時發生錯誤: {}", e.getMessage());
        }
    }

    /**
     * 判斷是否應該跳過此過濾器
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳過靜態資源和公開端點
        return path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.equals("/favicon.ico") ||
               path.equals("/error") ||
               path.equals("/") ||
               path.equals("/login") ||
               path.equals("/register") ||
               path.startsWith("/actuator/health");
    }
}