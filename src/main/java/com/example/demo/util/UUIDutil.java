//將密碼進行加密(使用者密碼不會直接顯示在資料庫)
package com.example.demo.util;

import java.util.UUID;

public class UUIDutil {
    //static靜態的方法
    public static String uuid(){
        //UUID.randomUUID()會產生一組隨機的UUID字串
        //toString()將UUID轉換成字串
        //replace("-", "")將字串中的"-"替換成空字串(也就是去掉"-")
        return UUID.randomUUID().toString().replace("-","");
    }
}
