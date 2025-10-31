//建立購物車介面
package com.example.demo.dao;

//import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.entity.Cart;
import com.example.demo.entity.CartVO;

public interface CartDao {
    //新增購物車商品
    Integer CreateCart(Cart cart);

    //判斷購物車有無商品
    List<Cart> FindByUidandPid(Integer uid,Integer pid);

    //更新購物車商品數量
    Integer UpdateNumByCid(Integer cid, Integer num, String updated_by, LocalDateTime updated_time);

    //根據uid查詢該用戶購物車商品
    List<CartVO> FindByUid(Integer uid);

    //根據cid查詢購物車商品
    Cart FindByCid(Integer cid);
    
    //根據cid刪除購物車商品
    Integer DeleteByCid(Integer cid);

    //根據uid刪除購物車商品
    Integer DeleteByUid(Integer uid);
}
