package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 簡化的安全配置 - 第一步實作
 * 
 * 步驟說明：
 * 1. 先建立基礎的安全配置
 * 2. 保持原有的金流功能正常運作
 * 3. 逐步添加 JWT 認證功能
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class BasicSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 第一步：建立基礎的安全過濾鏈
     * 
     * 目標：
     * - 保持現有功能不變
     * - 允許金流相關端點正常運作
     * - 為後續 JWT 整合做準備
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("🔧 正在配置基礎安全設定...");
        
        http
            // 步驟 1: 停用 CSRF（因為使用 API）
            .csrf(csrf -> {
                log.debug("停用 CSRF 保護");
                csrf.disable();
            })
            
            // 步驟 2: 配置授權規則
            .authorizeHttpRequests(auth -> {
                log.debug("設定授權規則");
                auth
                    // 完全公開的資源
                    .requestMatchers(
                        "/", "/index", "/product", "/register", "/login", "/mycarts",  // 網頁頁面
                        "/static/**", "/css/**", "/js/**", "/images/**",    // 靜態資源
                        "/favicon.ico", "/error"                           // 系統資源
                    ).permitAll()
                    
                    // 認證相關 API（JWT 登入、註冊等）
                    .requestMatchers(
                        "/api/auth/**",          // JWT 認證端點
                        "/register",             // 使用者註冊端點  
                        "/userlogin"             // 使用者登入端點
                    ).permitAll()
                    
                    // 金流相關端點（重要！保持現有邏輯）
                    .requestMatchers(
                        "/api/notify",           // NewWebPay 回調端點
                        "/api/pay/**"            // 支付相關 API
                    ).permitAll()
                    
                    // 健康檢查端點
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    
                    // 產品相關 API（允許公開訪問）
                    .requestMatchers("/products/**").permitAll()
                    
                    // 購物車相關 API（允許公開訪問，內部有 session 檢查）
                    .requestMatchers("/carts/**").permitAll()
                    
                    // 用戶相關 API（允許公開訪問）
                    .requestMatchers("/users/**").permitAll()
                    
                    // 測試端點（開發時期）
                    .requestMatchers("/api/test/public").permitAll()
                    
                    // 其他所有請求需要認證
                    .anyRequest().authenticated();
            })
            
            // 步驟 3: 使用表單登入（保持原有行為）
            .formLogin(form -> {
                log.debug("配置表單登入");
                form
                    .loginPage("/login")           // 自定義登入頁面
                    .defaultSuccessUrl("/login_success", true)  // 登入成功後跳轉
                    .permitAll();
            })
            
            // 步驟 4: 配置登出
            .logout(logout -> {
                log.debug("配置登出功能");
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .permitAll();
            })
            
            // 步驟 5: 加入 JWT 認證過濾器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 步驟 6: 配置 session 策略（允許 session 用於購物車等功能）
            .sessionManagement(session -> {
                log.debug("配置 Session 管理");
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                session.maximumSessions(1); // 每個用戶最多一個 session
            });

        log.info("✅ 基礎安全設定配置完成");
        return http.build();
    }

    /**
     * 密碼編碼器
     * 使用 BCrypt 進行密碼加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("🔐 配置密碼編碼器: BCrypt");
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 認證管理器
     * 用於 JWT 認證控制器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("🔐 配置認證管理器");
        return config.getAuthenticationManager();
    }
}