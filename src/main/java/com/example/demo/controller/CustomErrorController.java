package com.example.demo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定義錯誤處理控制器
 * 處理所有未捕獲的錯誤，包括 405 Method Not Allowed
 */
@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object method = request.getMethod();
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        Map<String, Object> errorDetails = new HashMap<>();
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            // 記錄錯誤信息
            log.warn("🚨 錯誤處理 - 狀態碼: {}, 方法: {}, URI: {}, 訊息: {}", 
                     statusCode, method, uri, message);
            
            errorDetails.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            errorDetails.put("status", statusCode);
            errorDetails.put("path", uri);
            errorDetails.put("method", method);
            
            // 根據不同的錯誤狀態碼提供友好的錯誤信息
            switch (statusCode) {
                case 404:
                    errorDetails.put("error", "Not Found");
                    errorDetails.put("message", "請求的資源不存在");
                    errorDetails.put("detail", "請檢查 URL 路徑是否正確");
                    break;
                case 405:
                    errorDetails.put("error", "Method Not Allowed");
                    errorDetails.put("message", "HTTP 方法不被允許");
                    errorDetails.put("detail", "該端點不支持 " + method + " 方法");
                    errorDetails.put("allowedMethods", getAllowedMethods(uri.toString()));
                    break;
                case 403:
                    errorDetails.put("error", "Forbidden");
                    errorDetails.put("message", "訢問被拒絕");
                    errorDetails.put("detail", "您沒有權限訪問此資源");
                    break;
                case 401:
                    errorDetails.put("error", "Unauthorized");
                    errorDetails.put("message", "未授權訪問");
                    errorDetails.put("detail", "請先登入或提供有效的認證資訊");
                    break;
                case 500:
                    errorDetails.put("error", "Internal Server Error");
                    errorDetails.put("message", "伺服器內部錯誤");
                    errorDetails.put("detail", "請聯繫管理員或稍後重試");
                    break;
                default:
                    errorDetails.put("error", "Unknown Error");
                    errorDetails.put("message", "未知錯誤");
                    errorDetails.put("detail", message != null ? message.toString() : "請聯繫技術支持");
            }
            
            return ResponseEntity.status(statusCode).body(errorDetails);
        }
        
        // 默認錯誤響應
        errorDetails.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorDetails.put("status", 500);
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", "伺服器發生未知錯誤");
        errorDetails.put("path", uri);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    /**
     * 根據 URI 路徑提供允許的 HTTP 方法建議
     */
    private String getAllowedMethods(String uri) {
        if (uri == null) return "GET";
        
        // API 端點的方法建議
        if (uri.startsWith("/api/auth/")) {
            if (uri.contains("/login") || uri.contains("/register")) {
                return "POST";
            } else if (uri.contains("/refresh") || uri.contains("/logout")) {
                return "POST, DELETE";
            }
            return "GET, POST";
        }
        
        // 商品相關端點
        if (uri.startsWith("/products/")) {
            return "GET";
        }
        
        // 用戶相關端點
        if (uri.startsWith("/user/")) {
            return "GET, POST, PUT";
        }
        
        // 默認允許 GET 方法
        return "GET";
    }
}