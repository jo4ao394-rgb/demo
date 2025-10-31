package com.example.demo.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtAuthenticationEntryPoint;
import com.example.demo.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * 混合認證安全配置
 * 支援 Session 和 JWT 雙重認證機制
 * 
 * 使用場景：
 * - Session: 傳統網頁、金流頁面、管理後台
 * - JWT: API 端點、行動應用、微服務間通信
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("hybrid") // 只在 hybrid profile 時啟用
public class HybridSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 停用 CSRF（API 不需要，但保留 Session 支援）
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/auth/**")
            )
            
            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 混合 Session 策略：API 使用無狀態，網頁使用 Session
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            
            // 配置授權規則
            .authorizeHttpRequests(auth -> auth
                // 完全公開的端點
                .requestMatchers(
                    "/login", "/register",           // 登入註冊頁面
                    "/", "/index", "/product",       // 首頁和產品頁面
                    "/static/**", "/css/**", "/js/**", "/images/**", // 靜態資源
                    "/api/auth/**",                  // JWT 認證端點
                    "/api/public/**",                // 公開 API
                    "/api/notify",                   // 金流回調（重要！）
                    "/actuator/health",              // 健康檢查
                    "/error"
                ).permitAll()
                
                // 需要 Session 認證的端點（傳統金流相關）
                .requestMatchers(
                    "/login_success",                // 登入成功頁面
                    "/mycarts", "/carts/**",         // 購物車頁面
                    "/users/**",                     // 使用者管理頁面
                    "/api/pay/**",                   // 金流相關 API（保持 Session）
                    "/api/query_info",               // 查詢交易
                    "/api/close_trade"               // 退款申請
                ).hasAnyRole("USER", "ADMIN")
                
                // 需要 JWT 認證的 API 端點
                .requestMatchers(
                    "/api/v1/**",                    // 新版 API
                    "/api/mobile/**",                // 行動端 API
                    "/api/secure/**"                 // 安全 API
                ).authenticated()
                
                // 管理員端點
                .requestMatchers("/api/admin/**", "/actuator/**").hasRole("ADMIN")
                
                // 其他請求需要認證
                .anyRequest().authenticated()
            )
            
            // 配置異常處理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // 添加 JWT 認證過濾器（不影響 Session 認證）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允許的來源
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        
        // 允許的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // 允許的標頭
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 暴露的標頭
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // 允許發送 Cookie（Session 支援）
        configuration.setAllowCredentials(true);
        
        // 預檢請求的快取時間
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}