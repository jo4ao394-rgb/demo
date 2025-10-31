//定義用戶登入(服務)介面
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.entity.User;
import com.example.demo.dao.UserDao;
//
import com.example.demo.util.MD5util;
import com.example.demo.util.UUIDutil;

@Component //將此類別註冊為Spring容器的Bean
//先創一個介面，這裡是給介面進行定義(用戶登入)
public class UserServiceImpl implements UserService{

    @Autowired //自動注入 UserDao可執行用戶介面
    private UserDao userDao;

    //@Autowired //自動注入 RedisTemplate可進行Redis操作
    //private RedisTemplate redisTemplate;

    @Override //覆寫
    //創建用戶(判斷此會員是否為創辦過的會員，沒有的話將近加密，並且儲存創建時間)
    //回傳會以創建用戶的介面進行回傳
    public String CreateUser(User user) {
        //先檢查是否有重複名稱(如果沒有就可以設定密碼並進行加密)
        String username =user.getUsername();
        //如果查詢結果不為null，代表有重複名稱
        //我在別的檔案進行介面設定後，我要引用就是界面.介面內的內容

        if(username.isEmpty()){
            throw new IllegalArgumentException("用戶名稱不能為空");
        }

        if(userDao.ReadByUsername(username) != null){
            //IllegalArgumentExceptio拋出錯誤
            throw new IllegalArgumentException("用戶名稱重複");
        }
        //如果可以執行到這一行，代表用戶名稱沒有被使用
        //存取當下時間
        //Date now =new Date();

        //密碼加密
        String salt = UUIDutil.uuid(); //取用加密隨機碼
        user.setSalt(salt); //放入用戶資料中
        String md5Password = MD5util.md5(user.getPassword(),salt); //將密碼進行MD5加密
        user.setPassword(md5Password);

        //開始設置其他後台參數，時間，使用者等等
        user.setIsDelete(0); //0代表未刪除
        user.setCreatedBy(username);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedBy(username);
        user.setUpdatedTime(LocalDateTime.now());

        return userDao.CreateUser(user);
    }

    //用戶登入
    @Override //覆寫
    public List<User> login(String username, String password) {

        //新增陣列，來接受userDao.ReadByUsername的回傳資料(ArrayList<>()為空陣列)
        List<User> list=new ArrayList<>();
        list=userDao.ReadByUsername(username); //查詢使用者
        //因為只會有一筆，所以在陣列的第0個位置
    
        //判斷帳號是否有被啟用(這裡原本順序有問題，已經調整過)
        if(list == null){
            //  拋出錯誤
            throw new IllegalArgumentException("登入失敗，找不到帳號");
        }

        User user= list.get(0);

        if(user.getIsDelete()==1){
            throw new IllegalArgumentException("登入失敗，帳號已經被刪除");
        }
        
        //判斷該使用者前端輸入的密碼與後端資料庫是否符合
        String salt = user.getSalt();
        String md5Password = MD5util.md5(password,salt);
        if(!user.getPassword().equals(md5Password)){
            throw new IllegalArgumentException("密碼輸入錯誤，請重新輸入");
        }

        return list; //回傳該使用者
    }

    @Override //覆寫
    //根據用戶名稱username查詢數據
    public List<User> ReadByUsername(String username) {
        return userDao.ReadByUsername(username);
    }

    //修改
    @Override
    public String UpdateByUid(Integer uid, User user) {

        if(userDao.ReadByUid(uid)== null){
            //  拋出錯誤
            throw new IllegalArgumentException("獲取數據失敗，請再重新嘗試一次");
        }
        //新增陣列，來接受userDao.ReadByUsername的回傳資料
        List<User> list=new ArrayList<>();
        list=userDao.ReadByUid(uid);
        //因為只會有一筆，所以在陣列的第0個位置
        User olduser= list.get(0);
        
        //判斷帳號否有被啟用
        if(olduser.getIsDelete()==1){
            throw new IllegalArgumentException("登入失敗，帳號已經被刪除");
        }
        
        //Date now =new Date();

        //更新用戶資料 - 檢查所有欄位不能為空且不能與原資料完全相同(這裡改過，之後可以更改密碼跟帳號，以id為主要判斷)
        // 檢查是否所有欄位都與原資料相同
        if (user.getEmail().equals(olduser.getEmail()) && 
            user.getPhone().equals(olduser.getPhone()) && 
            user.getGender() == olduser.getGender()) {
            throw new IllegalArgumentException("所有資料與原資料相同，無需更新");
        }
        
        // 更新各個欄位
        olduser.setEmail(user.getEmail());
        olduser.setPhone(user.getPhone());
        olduser.setGender(user.getGender());

        olduser.setUpdatedTime(LocalDateTime.now());
        // 使用原有用戶名稱作為更新者，避免 null 值
        olduser.setUpdatedBy(olduser.getUsername());

        return userDao.UpdateByUid(uid,olduser);
    }
}
