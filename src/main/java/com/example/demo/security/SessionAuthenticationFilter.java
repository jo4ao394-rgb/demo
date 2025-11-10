package com.example.demo.security;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Session 認證恢復過濾器
 * 從 HTTP Session 中恢復 Spring Security 認證上下文
 */
@Slf4j
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 如果已經有認證上下文，直接跳過
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 嘗試從 session 恢復認證上下文
        HttpSession session = request.getSession(false);
        if (session != null) {
            SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
            if (securityContext != null && securityContext.getAuthentication() != null) {
                log.debug("從 session 恢復認證上下文，用戶: {}", 
                         securityContext.getAuthentication().getName());
                SecurityContextHolder.setContext(securityContext);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}