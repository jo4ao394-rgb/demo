package com.example.demo.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 改善版安全配置 - 修復安全漏洞
 * 
 * 主要改善：
 * 1. 修復支付 API 安全性問題
 * 2. 增加 CSRF 保護（選擇性）
 * 3. 增加 CORS 配置
 * 4. 增加安全標頭
 */
@Slf4j
@Configuration
@EnableWebSecurity
@Profile("improved")
@RequiredArgsConstructor
public class ImprovedSecurityConfig {

    @Bean
    public SecurityFilterChain improvedFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 正在配置改善版安全設定...");
        
        http
            // 改善 1: 選擇性停用 CSRF（只對特定 API 停用）
            .csrf(csrf -> {
                log.debug("配置 CSRF 保護");
                csrf.ignoringRequestMatchers(
                    "/api/notify",      // 金流回調（第三方無法提供 CSRF token）
                    "/api/auth/**"      // JWT 認證 API
                );
            })
            
            // 改善 2: 增加 CORS 配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 改善 3: 增加安全標頭
            .headers(headers -> {
                log.debug("配置安全標頭");
                headers
                    .frameOptions(frame -> frame.deny())                    // 防止 clickjacking
                    .contentTypeOptions(Customizer.withDefaults())          // 防止 MIME sniffing
                    .httpStrictTransportSecurity(hsts -> 
                        hsts.maxAgeInSeconds(31536000)                      // HTTPS 強制
                    );
            })
            
            // 改善 4: 更精確的授權規則
            .authorizeHttpRequests(auth -> {
                log.debug("設定改善版授權規則");
                auth
                    // 完全公開的資源（無風險）
                    .requestMatchers(
                        "/", "/index", "/product", "/register", "/login",  
                        "/static/**", "/css/**", "/js/**", "/images/**",    
                        "/favicon.ico", "/error"                           
                    ).permitAll()
                    
                    // 第三方回調端點（必須公開）
                    .requestMatchers("/api/notify").permitAll()
                    
                    // 測試端點（明確分類）
                    .requestMatchers(
                        "/api/test/public", 
                        "/api/test/payment-callback",
                        "/api/simple/public/**"
                    ).permitAll()
                    
                    // 健康檢查（可考慮限制 IP）
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    
                    // ⚠️ 重要修正：支付相關 API 需要認證
                    .requestMatchers("/api/pay/**").authenticated()
                    
                    // JWT 認證相關（部分需要認證，部分公開）
                    .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
                    .requestMatchers("/api/auth/**").authenticated()
                    
                    // 其他受保護的 API
                    .requestMatchers("/api/test/protected", "/api/simple/protected/**").authenticated()
                    
                    // 管理員專用
                    .requestMatchers("/actuator/**").hasRole("ADMIN")
                    
                    // 其他所有請求需要認證
                    .anyRequest().authenticated();
            })
            
            // 表單登入配置
            .formLogin(form -> {
                log.debug("配置表單登入");
                form
                    .loginPage("/login")           
                    .defaultSuccessUrl("/login_success", true)  
                    .permitAll();
            })
            
            // 登出配置
            .logout(logout -> {
                log.debug("配置登出功能");
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)     // 清除 session
                    .deleteCookies("JSESSIONID")     // 刪除 session cookie
                    .permitAll();
            })
            
            // 改善 5: 異常處理
            .exceptionHandling(ex -> {
                log.debug("配置異常處理");
                ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        log.warn("未認證訪問: {} from {}", request.getRequestURI(), 
                                request.getRemoteAddr());
                        response.sendError(401, "需要認證");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        log.warn("權限不足: {} from {}", request.getRequestURI(), 
                                request.getRemoteAddr());
                        response.sendError(403, "權限不足");
                    });
            });

        log.info("✅ 改善版安全設定配置完成");
        return http.build();
    }

    /**
     * CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("🌐 配置 CORS 設定");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允許的來源（生產環境應該限制具體域名）
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://*.yourdomain.com"));
        
        // 允許的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允許的標頭
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        
        // 暴露的標頭
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // 允許發送 Cookie（Session 支援）
        configuration.setAllowCredentials(true);
        
        // 預檢請求快取時間
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("🔐 配置密碼編碼器: BCrypt (強度 12)");
        return new BCryptPasswordEncoder(12);
    }
}