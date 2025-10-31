//處理商品未找到的異常情況(這個不需要)
package com.example.demo.exception;

//RuntimeException的子類，用於表示商品未找到的異常情況
public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String message) {
        super(message); //傳入一段自訂訊息，並回傳
    }
}