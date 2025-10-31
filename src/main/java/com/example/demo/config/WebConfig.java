package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.interceptor.RateLimitInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Web MVC 配置
 * 註冊各種攔截器和處理器
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;
    
    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 註冊原有的登入檢查攔截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/login_success")
                .addPathPatterns("/carts/**")
                .addPathPatterns("/users/**");
                
        // 註冊限流攔截器 - 所有 API 請求
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                );
    }
}