//用來處理異常情況(例如:用戶名已存在、用戶名或密碼錯誤等)用法固定
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle(IllegalArgumentException exception) {
        //return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                //.body("IllegalArgumentException：" + exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(exception.getMessage());
    }
}
