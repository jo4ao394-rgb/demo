package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 testpass123 的雜湊
        String testpass123Hash = encoder.encode("testpass123");
        System.out.println("testpass123: " + testpass123Hash);
        
        // 生成 admin123 的雜湊
        String admin123Hash = encoder.encode("admin123");
        System.out.println("admin123: " + admin123Hash);
        
        // 驗證
        System.out.println("testpass123 matches: " + encoder.matches("testpass123", testpass123Hash));
        System.out.println("admin123 matches: " + encoder.matches("admin123", admin123Hash));
    }
}