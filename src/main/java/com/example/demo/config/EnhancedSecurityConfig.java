package com.example.demo.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.EnhancedJwtAuthenticationFilter;
import com.example.demo.security.JwtAuthenticationEntryPoint;
import com.example.demo.security.SessionAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 增強的安全配置 - JWT + Redis 混合方案
 * 
 * 功能特點：
 * 1. JWT + Session 混合認證
 * 2. Redis 作為 Token 黑名單存儲
 * 3. 針對性的 CSRF 保護
 * 4. 完整的安全標頭配置
 * 5. 細粒度的 API 端點保護
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
@Profile("enhanced") // 使用 enhanced profile 啟用
public class EnhancedSecurityConfig {

    private final EnhancedJwtAuthenticationFilter enhancedJwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final SessionAuthenticationFilter sessionAuthenticationFilter;

    @Bean
    public SecurityFilterChain enhancedFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 正在配置增強版安全設定...");
        
        http
            // 1. CORS 配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. 針對性 CSRF 保護
            .csrf(csrf -> {
                log.debug("配置針對性 CSRF 保護");
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(
                        "/api/**",           // API 端點使用 JWT，不需要 CSRF
                        "/userlogin",        // 登入端點
                        "/register",         // 註冊端點
                        "/api/auth/**",      // JWT 認證端點
                        "/api/pay/**",       // 支付回調端點
                        "/carts/**"          // 購物車端點（支援 Session 認證）
                    );
            })
            
            // 3. 細粒度授權控制
            .authorizeHttpRequests(auth -> {
                log.debug("設定細粒度授權規則");
                auth
                    // 完全公開的資源
                    .requestMatchers(
                        "/", "/index", "/login", "/register",           // 基本頁面
                        "/static/**", "/css/**", "/js/**", "/images/**", // 靜態資源
                        "/favicon.ico", "/error", "/actuator/health"     // 系統資源
                    ).permitAll()
                    
                    // 認證相關端點（公開）
                    .requestMatchers(
                        "/api/auth/login",      // JWT 登入
                        "/api/auth/register",   // JWT 註冊
                        "/userlogin",           // 傳統登入
                        "/api/auth/refresh"     // Token 刷新
                    ).permitAll()
                    
                    // 產品相關（公開瀏覽，購買需認證）
                    .requestMatchers(
                        "/product",             // 商品詳情頁面（公開）
                        "/products/list/**",    // 產品列表（公開）
                        "/products/image/**"    // 產品圖片（公開）
                    ).permitAll()
                    .requestMatchers("/products/**").authenticated() // 其他產品操作需認證
                    
                    // 金流相關（特殊處理）
                    .requestMatchers(
                        "/api/notify",          // 第三方支付回調
                        "/api/pay/callback"     // 支付回調
                    ).permitAll()
                    .requestMatchers("/api/pay/**").authenticated() // 其他支付操作需認證
                    
                    // 用戶相關（需要認證）
                    .requestMatchers(
                        "/users/session-username"  // Session 檢查（公開）
                    ).permitAll()
                    .requestMatchers("/users/**").authenticated() // 其他用戶操作需認證
                    
                    // 購物車相關（需要認證）
                    .requestMatchers("/carts/**", "/mycarts").authenticated()
                    
                    // 管理相關（需要 ADMIN 角色）
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    
                    // 其他所有請求需要認證
                    .anyRequest().authenticated();
            })
            
            // 4. 表單登入配置
            .formLogin(form -> {
                log.debug("配置表單登入");
                form
                    .loginPage("/login")
                    .defaultSuccessUrl("/login_success", true)
                    .permitAll();
            })
            
            // 5. 登出配置
            .logout(logout -> {
                log.debug("配置登出功能");
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll();
            })
            
            // 6. 認證過濾器（Session + JWT）
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(enhancedJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 7. 異常處理
            .exceptionHandling(ex -> {
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint);
            })
            
            // 8. Session 管理（混合模式）
            .sessionManagement(session -> {
                log.debug("配置混合 Session 管理");
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                       .maximumSessions(3) // 允許多設備登入
                       .maxSessionsPreventsLogin(false)
                       .sessionRegistry(sessionRegistry());
            })
            
            // 9. 安全標頭配置
            .headers(headers -> {
                log.debug("配置安全標頭");
                headers
                    // X-Frame-Options
                    .frameOptions(frameOptions -> frameOptions.deny())
                    // X-Content-Type-Options
                    .contentTypeOptions(Customizer.withDefaults())
                    // X-XSS-Protection
                    .addHeaderWriter((request, response) -> {
                        response.addHeader("X-XSS-Protection", "1; mode=block");
                    })
                    // Strict-Transport-Security (HSTS)
                    .httpStrictTransportSecurity(hstsConfig -> 
                        hstsConfig.maxAgeInSeconds(31536000) // 1 年
                                  .includeSubDomains(true)
                                  .preload(true)
                    )
                    // 自定義安全標頭
                    .addHeaderWriter((request, response) -> {
                        // Content-Security-Policy
                        response.addHeader("Content-Security-Policy", 
                            "default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline' cdn.jsdelivr.net cdnjs.cloudflare.com; " +
                            "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                            "img-src 'self' data:; " +
                            "font-src 'self' cdn.jsdelivr.net;");
                        // Referrer-Policy
                        response.addHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                        // Permissions-Policy
                        response.addHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
                    });
            });

        log.info("✅ 增強版安全設定配置完成");
        return http.build();
    }

    /**
     * CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://yourdomain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Session Registry Bean
     */
    @Bean
    public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    /**
     * 密碼編碼器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("🔐 配置密碼編碼器: BCrypt (強度: 12)");
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 認證管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("🔐 配置認證管理器");
        return config.getAuthenticationManager();
    }
}