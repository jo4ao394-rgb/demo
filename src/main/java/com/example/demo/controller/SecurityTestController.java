package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 安全測試控制器
 * 用於測試不同的認證場景
 * 
 * 步驟說明：
 * 1. 測試公開端點（無需認證）
 * 2. 測試需要認證的端點
 * 3. 驗證安全配置是否正確運作
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class SecurityTestController {

    /**
     * 公開端點測試
     * 任何人都可以訪問
     */
    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        log.info("🌐 公開端點被訪問");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "這是公開端點，無需認證");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 需要認證的端點測試
     * 需要登入才能訪問
     */
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint() {
        log.info("🔒 受保護端點被訪問");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "這是受保護的端點，需要認證");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", auth != null && auth.isAuthenticated());
        
        if (auth != null) {
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 金流回調模擬端點（應該無需認證）
     */
    @PostMapping("/payment-callback")
    public ResponseEntity<?> paymentCallback(@RequestBody(required = false) Map<String, Object> payload) {
        log.info("💳 模擬金流回調端點被訪問");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "金流回調端點正常運作");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "OK");
        response.put("received_data", payload);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 系統狀態檢查
     */
    @GetMapping("/status")
    public ResponseEntity<?> systemStatus() {
        log.info("📊 系統狀態檢查");
        
        Map<String, Object> response = new HashMap<>();
        response.put("application", "secure-ecommerce-api");
        response.put("version", "1.0.0");
        response.put("security_config", "basic");
        response.put("java_version", System.getProperty("java.version"));
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 錯誤處理測試
     */
    @GetMapping("/error-test")
    public ResponseEntity<?> errorTest() {
        log.warn("⚠️ 錯誤測試端點");
        throw new RuntimeException("這是一個測試錯誤");
    }
}