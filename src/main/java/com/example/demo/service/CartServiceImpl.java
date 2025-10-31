//定義購物車服務介面
package com.example.demo.service;

import java.time.LocalDateTime;
//import java.sql.Date;
//import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.dao.CartDao;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartVO;
import com.example.demo.entity.Product;

@Component
public class CartServiceImpl implements CartService {

    @Autowired//自動注入 CartDao可執行購物車介面
    private CartDao cartDao;

    @Autowired//自動注入 ProductService可執行商品服務介面
    private ProductService productService;

    //為購物車插入新的商品數據
    private void Create(Cart cart) {
        Integer rows = cartDao.CreateCart(cart);
        //判斷是加入成功
        if (rows != 1) {
            //改用了IllegalArgumentException而不是RuntimeException
            throw new IllegalArgumentException("創建購物車數據失敗");
        }
    }

    //查找購物車裡有無商品
    private List<Cart> FindByUidAndPid(Integer uid, Integer pid) {
        return cartDao.FindByUidandPid(uid,pid);
    }

    //更新購物車數據
    private void UpdateNumByCid(Integer cid, Integer num, String updated_by, LocalDateTime updated_time) {

        Integer rows = cartDao.UpdateNumByCid(cid, num, updated_by, updated_time);
        if (rows != 1) {
            //改用了IllegalArgumentException而不是RuntimeException
            throw new IllegalArgumentException("修改商品失敗");
        }
    }

    @Override //覆寫
    //購物車依uid顯示列表(顯示該用戶的購物車商品資料)
    public List<CartVO> GetByUid(Integer uid) {
        return FindByUid(uid);
    }

    private List<CartVO> FindByUid(Integer uid) {
        return cartDao.FindByUid(uid);
    }


    @Override //覆寫
    //將商品加入購物車
    public void AddToCart(Integer uid, String username, Integer pid, Integer amount) {
        //獲取目前時間
        //Date now = new Date();
        //查詢購物車情況，看看購物車是否有該商品
        //新增陣列，來接受FindByUidAndPid的回傳資料(ArrayList<>()為空陣列)
        List<Cart> list = new ArrayList<>();
        list=FindByUidAndPid(uid, pid); //找到該用戶的該商品
        //判斷是否為Null
        if (list == null) {
            // 是，表示需要創建購物車
            //獲取商品id
            Product product = productService.ReadById(pid); //根據商品id查詢商品數據

            // 檢查商品狀態和庫存(新增加的判斷)
            if (product.getStatus() == 0 || product.getNum() == 0 || product.getNum() < amount) {
                throw new IllegalArgumentException("商品已下架或庫存不足，無法加入購物車");
            }

            //創建新的物件
            Cart cart = new Cart();
            //開始設置數據
            cart.setUid(uid);
            cart.setPid(pid);
            cart.setNum(amount);
            cart.setPrice(product.getPrice());
            cart.setCreatedBy(username);
            cart.setCreatedTime(LocalDateTime.now());
            cart.setUpdatedBy(username);
            cart.setUpdatedTime(LocalDateTime.now());

            //創建購物車
            Create(cart);
        } else {
            Product product = productService.ReadById(pid);
            // 檢查商品狀態和庫存(新增加的判斷)
            if (product.getStatus() == 0 || product.getNum() == 0 || product.getNum() < list.get(0).getNum() + amount) {
                throw new IllegalArgumentException("商品已下架或庫存不足，無法加入購物車");
            }
            // 否，表示已經有了該商品，要更新數量
            //該用戶已經加入的商品，因為會按照數量計算總數所以已經是在第0筆，取得他的cid
            Integer cid = list.get(0).getCid(); 
            // 新數量=原數量 + 要加的新數量
            Integer num = list.get(0).getNum() + amount; //後面為要加入的數量
            // 更新
            cartDao.UpdateNumByCid(cid, num, username, LocalDateTime.now());
        }
    }

    @Override //覆寫
    //購物車數量加一(上面的可以自訂數量，這裡每次加1)
    public String AddNum(Integer cid, Integer uid, String username) {
        //根據cid查詢Cart數據
        Cart result = FindByCid(cid);
        //如果找不到數據
        if (result == null) {
            // 是：抛出CartNotFoundException
            //改用了IllegalArgumentException而不是CartNotFoundException
            throw new IllegalArgumentException("購物車不存在");
        }

        //查詢的Uid與session中的uid不匹配
        if (!result.getUid().equals(uid)) {
            //改用了IllegalArgumentException而不是CartNotFoundException
            throw new IllegalArgumentException("增加商品數量失敗，操作已被伺服器拒絕");
        }

        //新數量= 原數量 + 新數量
        Integer newNum = result.getNum() + 1;
        UpdateNumByCid(cid, newNum, username, LocalDateTime.now());

        return "新增成功";
    }
    
    @Override //覆寫
    //刪除購物車中的指定商品
    public String DeleteCart(Integer cid, Integer uid) {
        //根據cid查詢Cart數據
        Cart result = FindByCid(cid);
        //如果找不到數據
        if (result == null) {
            throw new IllegalArgumentException("購物車項目不存在");
        }
        
        //查詢的Uid與session中的uid不匹配
        if (!result.getUid().equals(uid)) {
            throw new IllegalArgumentException("刪除失敗，操作已被伺服器拒絕");
        }
        
        //執行刪除
        Integer rows = cartDao.DeleteByCid(cid);
        if (rows != 1) {
            throw new IllegalArgumentException("刪除購物車商品失敗");
        }
        
        return "刪除成功";
    }


    private Cart FindByCid(Integer cid) {
        return cartDao.FindByCid(cid);
    } //私人的方法


    @Override //覆寫
    //刪除該用戶的所有購物車商品
    public String DeleteUidCart(Integer uid) {
        //根據uid查詢Cart數據
        List<CartVO> result = cartDao.FindByUid(uid);

        //如果找不到數據或購物車為空，直接返回成功
        if (result == null || result.isEmpty()) {
            return "購物車已經是空的，刪除成功";
        }

        //執行刪除
        Integer rows = cartDao.DeleteByUid(uid);
        // 檢查是否刪除成功（rows 應該大於 0，不一定是 1）
        if (rows > 0) {
            return "刪除成功，共刪除 " + rows + " 個商品";
        } else {
            throw new IllegalArgumentException("刪除購物車商品失敗");
        }
    }


}
