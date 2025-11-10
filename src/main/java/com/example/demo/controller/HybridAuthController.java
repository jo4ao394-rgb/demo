package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.JwtRedisTokenService;
import com.example.demo.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT + Session 混合認證控制器
 * 
 * 功能：
 * 1. JWT 登入/登出
 * 2. Token 刷新
 * 3. 多設備管理
 * 4. 安全登出（黑名單機制）
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Profile("enhanced") // 只在 enhanced 模式下啟用
public class HybridAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Autowired(required = false)
    private JwtRedisTokenService jwtRedisTokenService;

    /**
     * 用戶註冊端點
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("用戶註冊嘗試，用戶名: {}, IP: {}", request.getUsername(), getClientIp(httpRequest));
            
            // 返回簡單的成功消息（實際項目中應該實現真正的註冊邏輯）
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "註冊功能需要實現用戶服務");
            response.put("username", request.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("註冊失敗，用戶名: {}, 錯誤: {}", request.getUsername(), e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "註冊失敗");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * JWT 登入端點
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("JWT 登入嘗試，用戶名: {}, IP: {}", request.getUsername(), getClientIp(httpRequest));

            // 1. 驗證用戶憑證
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2. 載入用戶詳細資訊
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            // 3. 生成 JWT Token
            String jwtId = UUID.randomUUID().toString();
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("ip", getClientIp(httpRequest));
            extraClaims.put("userAgent", httpRequest.getHeader("User-Agent"));
            
            String accessToken = jwtService.generateTokenWithJti(extraClaims, userDetails, jwtId);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // 4. Redis 記錄管理
            if (jwtRedisTokenService != null) {
                long expiration = jwtService.getExpirationTime(accessToken);
                jwtRedisTokenService.storeUserToken(request.getUsername(), jwtId, expiration);
                jwtRedisTokenService.storeRefreshToken(refreshToken, request.getUsername(), 
                    System.currentTimeMillis() + 604800000L); // 7天
            }

            // 5. 建立響應
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 86400); // 24小時（秒）
            response.put("username", request.getUsername());
            
            if (jwtRedisTokenService != null) {
                response.put("activeDevices", jwtRedisTokenService.getUserActiveTokenCount(request.getUsername()));
            }

            log.info("JWT 登入成功，用戶: {}", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("JWT 登入失敗，用戶名: {}, 錯誤: {}", request.getUsername(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "登入失敗");
            errorResponse.put("message", "用戶名或密碼錯誤");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * JWT Token 刷新
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            log.info("Token 刷新請求");

            // 1. 驗證 Refresh Token
            String username = null;
            if (jwtRedisTokenService != null) {
                username = jwtRedisTokenService.validateRefreshToken(request.getRefreshToken());
            }

            if (username == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "無效的 Refresh Token"));
            }

            // 2. 載入用戶資訊
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 3. 生成新的 Access Token
            String newJwtId = UUID.randomUUID().toString();
            String newAccessToken = jwtService.generateTokenWithJti(new HashMap<>(), userDetails, newJwtId);

            // 4. 更新 Redis 記錄
            if (jwtRedisTokenService != null) {
                long expiration = jwtService.getExpirationTime(newAccessToken);
                jwtRedisTokenService.storeUserToken(username, newJwtId, expiration);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 86400);

            log.info("Token 刷新成功，用戶: {}", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token 刷新失敗: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Token 刷新失敗"));
        }
    }

    /**
     * JWT 登出（單設備）
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request, HttpServletRequest httpRequest) {
        try {
            String token = request.getAccessToken();
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String username = jwtService.extractUsername(token);
            log.info("JWT 登出請求，用戶: {}", username);

            // 1. 將 Token 加入黑名單
            if (jwtRedisTokenService != null) {
                jwtRedisTokenService.blacklistToken(token);
                
                // 2. 從用戶 Token 集合中移除
                String jti = jwtService.getJwtId(token);
                jwtRedisTokenService.removeUserToken(username, jti);
                
                // 3. 移除 Refresh Token（如果提供）
                if (request.getRefreshToken() != null) {
                    jwtRedisTokenService.removeRefreshToken(request.getRefreshToken());
                }
            }

            // 4. 清除 Session（如果存在）
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            log.info("JWT 登出成功，用戶: {}", username);
            return ResponseEntity.ok(Map.of("message", "登出成功"));

        } catch (Exception e) {
            log.error("JWT 登出失敗: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "登出失敗"));
        }
    }

    /**
     * 登出所有設備
     */
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices(@RequestBody LogoutRequest request) {
        try {
            String token = request.getAccessToken();
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String username = jwtService.extractUsername(token);
            log.info("登出所有設備請求，用戶: {}", username);

            // 撤銷用戶的所有 Token
            if (jwtRedisTokenService != null) {
                jwtRedisTokenService.revokeAllUserTokens(username);
            }

            log.info("所有設備登出成功，用戶: {}", username);
            return ResponseEntity.ok(Map.of("message", "所有設備已登出"));

        } catch (Exception e) {
            log.error("登出所有設備失敗: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失敗"));
        }
    }

    /**
     * 獲取用戶活躍設備數量
     */
    @PostMapping("/devices")
    public ResponseEntity<?> getActiveDevices(@RequestBody String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String username = jwtService.extractUsername(token);
            
            Long activeCount = 0L;
            if (jwtRedisTokenService != null) {
                activeCount = jwtRedisTokenService.getUserActiveTokenCount(username);
            }

            return ResponseEntity.ok(Map.of(
                "username", username,
                "activeDevices", activeCount
            ));

        } catch (Exception e) {
            log.error("獲取活躍設備數量失敗: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失敗"));
        }
    }

    /**
     * 獲取客戶端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // DTO 類別
    public static class LoginRequest {
        private String username;
        private String password;

        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LogoutRequest {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}