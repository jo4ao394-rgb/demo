//規劃本專案所有的Controller(也就是會回傳段硬的畫面顯示出來)
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@org.springframework.stereotype.Controller
public class Controller {
    @GetMapping("/login")
    public String loginpage(Model model){
        return "login";
    }

    //登入頁面
    @GetMapping("/login_success")
    public String successpage(Model model){
        return "login_success";
    }

    //首頁
    @GetMapping("/")
    public String index(Model model, HttpSession session){
        return "index";
    }

    //註冊頁面
    @GetMapping("/register")
    public String register(Model model){
        return "register";
    }

    //user修改資料頁面
    @GetMapping("/users/@{username}")
    public String usermodified(Model model){
        return "user_modified";
    }

    //商品詳情頁面
    @GetMapping("/product")
    public String products(Model model){
        return "product";
    }


    //購物車詳情頁面
    @GetMapping("/mycarts")
    public String mycarts(Model model){
        return "mycarts";
    }

}