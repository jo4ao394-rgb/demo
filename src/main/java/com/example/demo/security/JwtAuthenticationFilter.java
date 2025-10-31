package com.example.demo.security;

import java.io.IOException;

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
 * JWT 認證過濾器
 * 攔截每個 HTTP 請求並驗證 JWT Token
 * 
 * 功能包括：
 * - 從請求標頭中提取 JWT Token
 * - 驗證 Token 的有效性
 * - 設定 Spring Security 認證上下文
 * - 處理 Token 異常情況
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 檢查是否為需要跳過的路徑
        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 檢查 Authorization 標頭格式
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 提取 JWT Token（移除 "Bearer " 前綴）
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            // 如果用戶名存在且未認證
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 驗證 Token 有效性
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // 檢查 Token 是否即將過期，記錄日誌以便提醒更新
                    if (jwtService.isTokenAboutToExpire(jwt)) {
                        log.warn("JWT Token 即將過期，使用者: {}", userEmail);
                    }
                } else {
                    log.warn("無效的 JWT Token，使用者: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("JWT 認證過程中發生錯誤: {}", e.getMessage());
            // 清除可能存在的認證資訊
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判斷是否應該跳過該過濾器
     * @param request HTTP 請求
     * @return 是否跳過
     */
    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳過這些路徑的 JWT 認證檢查（使用 Session 認證）
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/api/pay/") ||          // 金流相關 API 使用 Session
               path.startsWith("/api/notify") ||         // 金流回調
               path.startsWith("/api/query_info") ||     // 查詢交易
               path.startsWith("/api/close_trade") ||    // 退款申請
               path.equals("/actuator/health") ||
               path.equals("/actuator/info") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/login") ||
               path.startsWith("/register") ||
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.equals("/error") ||
               path.equals("/favicon.ico") ||
               path.equals("/") ||
               path.equals("/index") ||
               path.equals("/product");
    }
}