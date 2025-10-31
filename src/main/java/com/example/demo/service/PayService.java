//負責組合請求、呼叫 NewWebUtil、解析回傳、更新訂單。
package com.example.demo.service;

import com.example.demo.entity.CartVO;
import com.example.demo.newwebpay.NewWebUtil;
import com.example.demo.newwebpay.bean.PayResponse;
import com.example.demo.newwebpay.bean.PaymentRequest;
import com.example.demo.newwebpay.bean.PaymentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Transactional
public class PayService {

    @PersistenceContext
    private EntityManager entityManager;
    //(改)
    
    private final OrderService orderService;
    private final OrderListService orderListService;
    private final CartService cartService;


    public PayService(OrderService orderService, OrderListService orderListService, CartService cartService) {
        this.orderService = orderService;
        this.orderListService = orderListService;
        this.cartService = cartService;
    }

    @Value("${new-web-pay.key}")
    private String key; //藍新提供的 HashKey

    @Value("${new-web-pay.iv}")
    private String iv; //藍新提供的 HashIV

    @Value("${new-web-pay.mid}")
    private String mid; //商店代號

    @Value("${new-web-pay.pay-url}")
    private String payUrl; //付款網址

    @Value("${new-web-pay.query-trade-info-url}")
    private String queryTradeInfoUrl; //查詢訂單網址

    @Value("${new-web-pay.close-url}")
    private String closeUrl; //退款網址

    @Value("${new-web-pay.notify-url}")
    private String notifyUrl; //付款完成通知網址

    //送出訂單
    //建立一筆付款請求並回傳前端需要的付款資料(改)
    public PayResponse pay(int totalAmount, String itemDescription, Integer uid) {
        String merchantOrderNo = getRandomString(15);
        
        // 創建總訂單記錄(改)
        orderService.createOrder(merchantOrderNo, totalAmount, itemDescription, uid);

        // 創建明細訂單記錄(改)
        // 取得該用戶購物車所有商品
        List<CartVO> cartItems = cartService.GetByUid(uid);
        //一一記錄該資料明細所有商品
        for (CartVO cart : cartItems) {
            //先找到第一筆商品
            orderListService.createOrderLList(merchantOrderNo, cart.getPrice(), cart.getNum(), cart.getPid(), uid);
        }     
   
        PaymentRequest request = new PaymentRequest(mid, key, iv, merchantOrderNo, totalAmount, itemDescription, notifyUrl);
        return PayResponse.of(request, payUrl);
    }

/*
    public PayResponse pay() {
        PaymentRequest request = new PaymentRequest(mid, key, iv, getRandomString(15), 1000, "Test測試購買", notifyUrl);
        return PayResponse.of(request, payUrl);
    }

    //購物車結帳功能 - 使用自定義金額和商品描述(改)
    public PayResponse checkoutCart(int totalAmount, String itemDescription) {
        String merchantOrderNo = "CART_" + getRandomString(10); // 購物車訂單編號：前綴 + 隨機字串
        PaymentRequest request = new PaymentRequest(mid, key, iv, merchantOrderNo, totalAmount, itemDescription, notifyUrl);
        return PayResponse.of(request, payUrl);
    }
*/
    //接收藍新金流回傳資料
    //完成付款後，藍新金流會「主動」呼叫你的 Notify URL
    //負責接收與處理那個通知（Webhook）
    public void notify(String tradeInfo) {
        String response = NewWebUtil.decryptAES(tradeInfo, key, iv);
        try {
            PaymentResponse.NotifyResponse notifyResponse = NewWebUtil.convertJson(response, PaymentResponse.NotifyResponse.class);
            log.info("notify->{}", notifyResponse.toString());

            //更新總訂單(改)
            //this代表目前正在執行的 PayService 物件本身
            orderService.updateOrderFromNotify(notifyResponse, this);
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    //查詢交易狀態
    public PaymentResponse.QueryTradeInfoResponse queryTradeInfo(String merchantOrderNo, Integer amt) {
        try {
            return new PaymentRequest.QueryTradeInfo(mid, key, iv, merchantOrderNo, amt).request(queryTradeInfoUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //交易要退款或取消付款
    public PaymentResponse.CloseTradeResponse closeTrade(String merchantOrderNo, Integer amt) {
        try {
            return new PaymentRequest.CloseTrade(mid, key, iv, merchantOrderNo, amt).request(closeUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //產生隨機字串用來生成訂單編號
    private static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}