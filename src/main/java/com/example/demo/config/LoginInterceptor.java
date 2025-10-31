//攔截器(登入成功的使用者ID存進Session)
package com.example.demo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component //將此類別註冊為Spring容器的Bean
//HandlerInterceptor是Spring MVC提供的一個介面，用於攔截HTTP請求
public class  LoginInterceptor implements HandlerInterceptor {

    @Override //覆寫
    //回傳布林值 reques前端請求 response後端回應 handler處理器(要執行的) Exception可能會拋出例外
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        //如果session中的uid為null，代表沒有登入
        if (request.getSession().getAttribute("uid") == null) {
            System.out.println("session中的uid為null");
            response.setStatus(302);
            return false;
        }
        System.out.println("session中的uid為= " + request.getSession().getAttribute("uid") + " - 允許訪問");
        return true;
    }
}
