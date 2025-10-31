//將藍新資料回傳至資料表(新建、更新)
package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.newwebpay.bean.PaymentResponse;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    @Autowired //自動注入
    private OrderRepository orderRepository;

    @Autowired //自動注入
    private CartService cartService;

    @Autowired //自動注入
    private OrderListService orderListService;
    
    // 創建新訂單(根據pay加密前的資料先新建)
    public Order createOrder(String merchantOrderNo, Integer totalAmount, String itemDescription, Integer uid) {
        Order order = new Order();
        order.setUid(uid);
        order.setMerchantorderno(merchantOrderNo);
        order.setTotalamt(totalAmount);
        order.setItemdescription(itemDescription);
        order.setTradestatus("0"); // 初始狀態：未付款
        order.setCreatedBy("SYSTEM");
        order.setUpdatedBy("SYSTEM");
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdatedTime(LocalDateTime.now());
        
        //利用資料庫介面將更新後的資料儲存到資料庫
        Order savedOrder = orderRepository.save(order);
        //回傳訊息到控制台
        log.info("創建新訂單：{}", savedOrder.getMerchantorderno());
        return savedOrder;
    }

    //更新訂單
    public void updateOrderFromNotify(PaymentResponse.NotifyResponse notifyResponse, PayService payService) {

        //取得付款完成通知的結果中的merchantOrderNo(訂單編號)
        PaymentResponse.NotifyResponse.Result result = notifyResponse.getResult();
        String merchantOrderNo = result.getMerchantOrderNo();

        //queryResponse有包含回傳的主要狀態成功還是失敗，.getResult()是那筆的詳細訊息不包含主要狀態
        //使用 queryTradeInfo 主動查詢即時完整的交易資訊
        PaymentResponse.QueryTradeInfoResponse queryResponse = payService.queryTradeInfo(merchantOrderNo, result.getAmt());
        //取出查詢結果中的詳細訊息
        PaymentResponse.QueryTradeInfoResponse.Result queryResult = queryResponse.getResult();
        //利用資料庫介面查詢對應merchantOrderNo(訂單編號)資料
        Optional<Order> orderOpt = orderRepository.findByMerchantorderno(merchantOrderNo);

        // 更新訂單資訊
        //isPresent判斷是否為空(有對應的訂單編號回傳true)
        if (orderOpt.isPresent() && queryResult != null) {
            //取出實際的訂單資料
            Order order = orderOpt.get();

            // 更新總訂單
            order.setStatus(notifyResponse.getStatus());
            order.setTradestatus(queryResult.getTradeStatus());
            order.setPaymenttype(queryResult.getPaymentType());
            order.setTradeno(queryResult.getTradeNo());
            order.setPaytime(queryResult.getPayTime());

            order.setUpdatedBy("SYSTEM");
            order.setUpdatedTime(LocalDateTime.now());


            //利用資料庫介面將更新後的資料儲存到資料庫
            orderRepository.save(order);            
            /**
             * 0=未付款
             * 1=付款成功
             * 2=付款失敗
             * 3=取消付款
             * 6=退款
             */    
            // 如果付款成功（交易狀態為 "1"），清空該用戶的購物車
            if ("0".equals(queryResult.getTradeStatus()) || "1".equals(queryResult.getTradeStatus()) || "2".equals(queryResult.getTradeStatus())) {
                try {
                    String deleteResult = cartService.DeleteUidCart(order.getUid());
                    log.info("交易完成，購物車清空結果：{}", deleteResult);
                } catch (Exception e) {
                    log.error("清空用戶 {} 購物車時發生錯誤：{}", order.getUid(), e.getMessage());
                }
            }

            // 更新明細訂單
            orderListService.updateOrderListFromNotify(queryResult);           

            //回傳訊息到控制台
            log.info("訂單更新成功：{}，新狀態：{}", merchantOrderNo, queryResult.getTradeStatus());
        } else {
            log.warn("找不到訂單：{}，或查詢交易資訊失敗，無法更新。訂單存在：{}，查詢回應：{}", 
                    merchantOrderNo, orderOpt.isPresent(), queryResponse != null);
        }
    }
/* 
    // 根據訂單編號查詢訂單
    public Optional<Order> findByMerchantOrderNo(String merchantOrderNo) {
        return orderRepository.findByMerchantorderno(merchantOrderNo);
    }
   
    // 檢查訂單是否存在
    public boolean existsByMerchantOrderNo(String merchantOrderNo) {
        return orderRepository.existsByMerchantorderno(merchantOrderNo);
    }
*/   
}