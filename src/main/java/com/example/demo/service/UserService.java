//建立用戶登入(服務)介面
package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.User;

public interface UserService {
    //創建用戶
    String CreateUser(User user);
    //根據用戶名稱username查詢數據
    List<User> ReadByUsername(String username);
    //登入
    List<User> login(String username,String password);
	//修改
    String UpdateByUid(Integer uid,User user);
}
