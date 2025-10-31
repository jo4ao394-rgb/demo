//負責接收前端或第三方（藍新）的請求，並呼叫 PayService
package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.CartVO;
import com.example.demo.newwebpay.bean.PayResponse;
import com.example.demo.newwebpay.bean.PaymentResponse;
import com.example.demo.service.CartService;
import com.example.demo.service.PayService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // 允許所有來源存取（開發測試用）
@RequiredArgsConstructor

public class PayController {

    private final PayService payService;

    @Autowired //自動注入 購物車服務介面
    private CartService cartService;

    /**
     * 送出訂單
     * POST http://localhost:8080/api/pay
     */

    @PostMapping("/pay")
    public ResponseEntity<PayResponse> pay(@RequestParam int totalAmount, HttpSession session) {
        //從session查詢uid數值、用戶名稱
        Integer uid = (Integer) session.getAttribute("uid");
        String username = (String) session.getAttribute("username");

        if (uid == null || username == null) {
            throw new IllegalArgumentException("用戶未登入");
        }

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
            throw new IllegalArgumentException("金額驗證失敗：前端金額=" + totalAmount + ", 實際金額=" + actualTotal);
        }

        //建立商品描述
        String itemDescription = "購物車商品 - 用戶: " + username + " (" + cartItems.size() + "件商品)";
        PayResponse response = payService.pay(actualTotal, itemDescription, uid);  // 使用驗證後的金額
        return ResponseEntity.ok(response);
    }

    /**
     * 單一訂單查詢
     * POST http://localhost:8080/api/query_info?
     *     merchantOrderNo=[merchantOrderNo]&
     *     amt=[amt]&
     *     Content-Type: application/x-www-form-urlencoded
     */
    @PostMapping("/query_info")
    public ResponseEntity<PaymentResponse.QueryTradeInfoResponse> queryTradeInfo(@RequestParam String merchantOrderNo, @RequestParam Integer amt, HttpSession session) {
        //從session查詢uid數值、用戶名稱
        Integer uid = (Integer) session.getAttribute("uid");
        String username = (String) session.getAttribute("username");

        if (uid == null || username == null) {
            throw new IllegalArgumentException("用戶未登入");
        }

        PaymentResponse.QueryTradeInfoResponse response = payService.queryTradeInfo(merchantOrderNo, amt);
        return ResponseEntity.ok(response);
    }

    /**
     * 請退款
     * POST http://localhost:8080/api/close_trade?
     *     merchantOrderNo=[merchantOrderNo]&
     *     amt=[amt]&
     *     Content-Type: application/x-www-form-urlencoded
     */
    @PostMapping("/close_trade")
    public ResponseEntity<PaymentResponse.CloseTradeResponse> closeTrade(@RequestParam String merchantOrderNo, @RequestParam Integer amt, HttpSession session) {
        //從session查詢uid數值、用戶名稱
        Integer uid = (Integer) session.getAttribute("uid");
        String username = (String) session.getAttribute("username");

        if (uid == null || username == null) {
            throw new IllegalArgumentException("用戶未登入");
        }
        
        PaymentResponse.CloseTradeResponse response = payService.closeTrade(merchantOrderNo, amt);
        return ResponseEntity.ok(response);
    }

    /**
     *  接收藍新金流回傳資料
     */
    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestParam(name = "Status", required = false) String status,
                                         @RequestParam(name = "MerchantID", required = false) String merchantId,
                                         @RequestParam(name = "TradeInfo", required = false) String tradeInfo,
                                         @RequestParam(name = "TradeSha", required = false) String tradeSha) {
        log.info("notify status->{}, message->{}, result->{}, tradeSha->{}", status, merchantId, tradeInfo, tradeSha);
        payService.notify(tradeInfo);
        return ResponseEntity.ok("OK");
    }
}