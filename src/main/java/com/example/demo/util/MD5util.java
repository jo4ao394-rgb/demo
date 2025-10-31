//將密碼進行MD5加密(使用者密碼不會直接顯示在資料庫)
package com.example.demo.util;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component //將此類別註冊為Spring容器的Bean
public class MD5util {
    //static靜態的方法
    public static String md5(String src ,String salt){
        //這邊使用了springframework的加密方式
        //md5DigestAsHex參數是Bytes，所以透過java String類將字串轉為Bytes
        String result=src + salt; //原始密碼加上加密隨機碼
        return DigestUtils.md5DigestAsHex(result.getBytes()); //進行MD5加密(就會變得跟原本不一樣了)
    }

}
