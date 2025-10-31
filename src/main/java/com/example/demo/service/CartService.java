//建立購物車服務介面
package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.CartVO;

public interface CartService {
    //將商品加入購物車
    void AddToCart(Integer uid, String username, Integer pid, Integer amount);

    //購物車依uid顯示列表
    List<CartVO> GetByUid(Integer uid);

    //購物車數量加一
    String AddNum(Integer cid, Integer uid, String username);
    
    //刪除指定購物車中的商品
    String DeleteCart(Integer cid, Integer uid);

    //刪除結帳後用戶購物車中商品
    String DeleteUidCart(Integer uid);

}
