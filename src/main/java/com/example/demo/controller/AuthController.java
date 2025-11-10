package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 認證控制器
 * 處理使用者登入、註冊和 Token 相關操作
 * 
 * 功能包括：
 * - 使用者登入驗證
 * - JWT Token 生成
 * - Token 刷新機制
 * - 登出處理
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Profile("!enhanced") // 只在非 enhanced 模式下啟用
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * 使用者登入
     * @param loginRequest 登入請求
     * @return JWT Token 響應
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("使用者登入嘗試: {}", loginRequest.getUsername());

            // 驗證使用者憑證
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 生成 JWT Token
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登入成功");
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());

            log.info("使用者登入成功: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("登入失敗: {} - {}", loginRequest.getUsername(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "登入失敗：帳號或密碼錯誤");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Token 刷新
     * @param refreshRequest 刷新請求
     * @return 新的 JWT Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            String username = jwtService.extractUsername(refreshToken);
            
            if (username != null && !jwtService.isTokenExpired(refreshToken)) {
                // 這裡應該從 UserDetailsService 載入使用者詳細資訊
                // 暫時省略具體實作
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Token 刷新成功");
                // response.put("accessToken", newAccessToken);
                
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Refresh token 無效或已過期");
            }
            
        } catch (Exception e) {
            log.error("Token 刷新失敗: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Token 刷新失敗");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 驗證 Token
     * @param token JWT Token
     * @return 驗證結果
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        try {
            String username = jwtService.extractUsername(token);
            boolean isExpired = jwtService.isTokenExpired(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", !isExpired);
            response.put("username", username);
            response.put("expired", isExpired);
            
            if (jwtService.isTokenAboutToExpire(token)) {
                response.put("warning", "Token 即將過期");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 登出
     * @return 登出結果
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 在實際專案中，這裡可以：
        // 1. 將 Token 加入黑名單
        // 2. 清除伺服器端的 Session
        // 3. 記錄登出事件
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        
        log.info("使用者登出");
        return ResponseEntity.ok(response);
    }
}

/**
 * 登入請求 DTO
 */
class LoginRequest {
    @NotBlank(message = "使用者名稱不能為空")
    private String username;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, message = "密碼長度至少 6 個字元")
    private String password;
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

/**
 * Token 刷新請求 DTO
 */
class RefreshTokenRequest {
    @NotBlank(message = "Refresh token 不能為空")
    private String refreshToken;
    
    // Getters and Setters
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}