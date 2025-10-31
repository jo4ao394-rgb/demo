package com.example.demo.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JPA 審計配置
 * 自動記錄資料的創建和修改資訊
 * 
 * 功能包括：
 * - 自動記錄創建時間和修改時間
 * - 自動記錄創建者和修改者
 * - 整合 Spring Security 獲取當前用戶
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    /**
     * 審計者提供者
     * 自動獲取當前登入的使用者作為審計者
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SecurityAuditorAware();
    }

    /**
     * 安全審計者實作
     */
    public static class SecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }
            
            return Optional.of(authentication.getName());
        }
    }
}