//執行購物車網頁操作
package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.http.ResponseEntity;

import com.example.demo.entity.CartVO;
import com.example.demo.service.CartService;
//import com.example.demo.service.PayService;
//import com.example.demo.newwebpay.bean.PayResponse;

import jakarta.servlet.http.HttpSession;

@RestController //返回值為Java物件，也就是說返回給前端JSON格式的數據(網頁位置設定用)

//@CrossOrigin(origins = "*")

@RequestMapping("/carts") //設定此類別的路徑為/carts
public class CartController {

    @Autowired //自動注入 購物車服務介面
    private CartService cartService;

    //@Autowired //自動注入 付款服務介面
    //private PayService payService;

    //http://localhost:8080/carts/addcart?pid=1&amount=3
    //將商品加入購物車
    @PostMapping("/addcart") //Post請求(新增資料)
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    public String AddToCart(Integer pid, Integer amount, HttpSession session){
        //從session查詢uid數值、用戶名稱
        Integer uid =(int) session.getAttribute("uid");
        String username =(String) session.getAttribute("username");
        cartService.AddToCart(uid, username, pid, amount);
        return "購物車操作成功";
    }

    //http://localhost:8080/carts/
    //該用戶結帳購物車列表
    @GetMapping("/") //Get請求(查詢資料)
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    public List<CartVO> GetByUid(HttpSession session) {
        //從sesion查詢uid數值
        Integer uid =(int) session.getAttribute("uid");
        //執行查詢並返回
        return cartService.GetByUid(uid);
    }

    //http://localhost:8080/carts/1/num/add
    //該用戶購物車商品數量加1
    @PostMapping("{cid}/num/add") //Post請求(新增資料)
    //@Path用來取得url路徑的值 PathVariable為直接將參數作為url一部份)
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    public String AddNum(@PathVariable("cid") Integer cid, HttpSession session) {
        //從session查詢uid數值、用戶名稱
        Integer uid =(int) session.getAttribute("uid");
        String username =(String) session.getAttribute("username");
        //執行查詢並返回
        return cartService.AddNum(cid, uid, username);
    }
    
    //http://localhost:8080/carts/1/delete
    //刪除購物車中的商品
    @PostMapping("{cid}/delete") //Post請求(刪除資料)
    //@Path用來取得url路徑的值 PathVariable為直接將參數作為url一部份)
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    public String DeleteCart(@PathVariable("cid") Integer cid, HttpSession session) {
        //從session查詢uid數值
        Integer uid = (Integer) session.getAttribute("uid");
        //執行查詢並返回
        return cartService.DeleteCart(cid, uid);
    }
/*
    //http://localhost:8080/carts/checkout
    //購物車結帳功能 - 整合藍新金流(改)
    //@RequestParam 獲取放在url後面的參數(@RequestParam為?參數
    //HttpSession用來設置session(代表用戶帶著憑證做事情)
    @PostMapping("/checkout") //Post請求(結帳處理)
    public ResponseEntity<PayResponse> checkoutCart(@RequestParam Integer totalAmount, HttpSession session) {
        //從session查詢用戶資訊
        Integer uid = (Integer) session.getAttribute("uid");
        String username = (String) session.getAttribute("username");
        
        if (uid == null || username == null) {
            throw new IllegalArgumentException("用戶未登入");
        }
        
        //檢查購物車是否有商品
        List<CartVO> cartItems = cartService.GetByUid(uid);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("購物車是空的");
        }
        
        //計算實際總金額(防止前端篡改)
        //stream() 將集合轉換為流
        //mapToInt 將每個CartVO物件映射為其價格乘以數量的整數值
        int actualTotal = cartItems.stream()
            .mapToInt(item -> item.getPrice() * item.getNum())
            .sum();
            
        if (actualTotal != totalAmount) {
            throw new IllegalArgumentException("金額驗證失敗");
        }
        
        //建立商品描述
        String itemDescription = "購物車商品 - 用戶: " + username + " (" + cartItems.size() + "件商品)";
        
        //呼叫藍新金流支付服務
        PayResponse payResponse = payService.checkoutCart(actualTotal, itemDescription);
        
        return ResponseEntity.ok(payResponse);
    }
*/
}