package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 簡單的無資料庫測試控制器
 * 用於測試安全配置是否正常運作
 */
@RestController
@RequestMapping("/api/simple")
public class SimpleTestController {

    /**
     * 完全公開的端點
     * 測試是否可以訪問
     */
    @GetMapping("/public/hello")
    public ResponseEntity<?> publicHello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "🎉 公開端點正常運作！");
        response.put("security", "無需認證");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 需要認證的端點
     * 應該會返回 401 或重新導向到登入頁面
     */
    @GetMapping("/protected/hello")
    public ResponseEntity<?> protectedHello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "🔒 受保護端點正常運作！");
        response.put("security", "需要認證");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 系統健康檢查
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("java_version", System.getProperty("java.version"));
        response.put("spring_profiles", System.getProperty("spring.profiles.active", "default"));
        response.put("security_config", "BasicSecurityConfig");
        
        return ResponseEntity.ok(response);
    }
}