//執行用戶網頁操作(註冊、登入、修改資料、登出)
package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController //返回值為Java物件，也就是說返回給前端JSON格式的數據(網頁位置設定用)
//@CrossOrigin(origins = "*") //跨網域專用
public class UserController {

    @Autowired //自動注入 用戶登入介面
    private UserService userService;

    //用戶註冊
    @PostMapping("/register") //Post請求(新增資料)
    //@RequestBody用來接收前端傳過來的JSON數據並轉換成Java物件
    public String create(@RequestBody User user){
        return userService.CreateUser(user); //進行註冊，回傳註冊成功或失敗
    }

    //用戶登入
    @PostMapping("/userlogin") //Post請求(新增資料)
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    //@RequestBody用來接收前端傳過來的JSON數據並轉換成Java物件
    public List<User> userlogin(HttpSession session, @RequestBody User user){
        //ArrayList<>()新增陣列，來接受userService.login的回傳資料(ArrayList<>()為空陣列)
        List<User> list =new ArrayList<>();
        list =userService.login(user.getUsername(),user.getPassword());
      	//登入成功後設置session的值
        session.setAttribute("uid",list.get(0).getUid());
        session.setAttribute("username",list.get(0).getUsername());

        return list;
    }


    @PutMapping("/users/{uid}") //Put請求(更新資料)根據帳號做修改
    //@Path用來取得url路徑的值 PathVariable為直接將參數作為url一部份)
    //@RequestBody用來接收前端傳過來的JSON數據並轉換成Java物件
    public String update(@PathVariable Integer uid,@RequestBody User user){ 
        return userService.UpdateByUid(uid,user);
    }

    @GetMapping("/users/{username}") //Get請求(查詢資料)
    //@Path用來取得url路徑的值 PathVariable為直接將參數作為url一部份)
    public List<User> read(@PathVariable String username){ 
        return userService.ReadByUsername(username);
    }

	//用來獲取session中的username，在前端某些需要的地方會用到。
    @GetMapping("/users/session-username") //Get請求(查詢資料)
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    public  String getsessionusername(HttpSession session){ 
        return (String)session.getAttribute("username");
    } //前端可以呼叫這個 API 拿到目前登入的使用者名稱回傳給前端

    //用戶登出
    @GetMapping("/sign_out") //Get請求(查詢資料)
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    public String signout(HttpSession session){
        //銷毀session中的KV
        session.removeAttribute("uid");
        session.removeAttribute("username");

        return "登出成功";
    }

}