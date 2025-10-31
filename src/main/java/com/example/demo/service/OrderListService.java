//定義用戶明細資料表(新建、更新)
package com.example.demo.service;

import com.example.demo.entity.OrderList;
import com.example.demo.newwebpay.bean.PaymentResponse;
import com.example.demo.repository.OrderListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.time.LocalDateTime;
//import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderListService {

    @Autowired //自動注入
    private OrderListRepository orderListRepository;

    @Autowired //自動注入
    private ProductService productService;

    // 創建新訂單(根據pay加密前的資料先新建)
    public OrderList createOrderLList(String merchantOrderNo, Integer amt, Integer num, Integer pid, Integer uid) {
        OrderList orderList = new OrderList();
        orderList.setUid(uid);
        orderList.setMerchantorderno(merchantOrderNo);
        orderList.setAmt(amt);
        orderList.setNum(num);
        orderList.setPid(pid);
        orderList.setTradestatus("0"); // 初始狀態：未付款

        //利用資料庫介面將更新後的資料儲存到資料庫
        OrderList savedOrderList = orderListRepository.save(orderList);
        //回傳訊息到控制台
        log.info("創建新訂單：{}", savedOrderList.getMerchantorderno());
        return savedOrderList;
    }

    public void updateOrderListFromNotify(PaymentResponse.QueryTradeInfoResponse.Result queryResult) {
        //取出查詢結果中的詳細訊息
        List<OrderList> orderLists = orderListRepository.findAllByMerchantorderno(queryResult.getMerchantOrderNo());

        // 更新所有該訂單編號的明細訂單
        if (!orderLists.isEmpty() && queryResult != null) {
            //一筆一筆更新
            for (OrderList orderList : orderLists) {
                // 更新每筆明細訂單
                orderList.setTradestatus(queryResult.getTradeStatus());
                orderList.setPaymenttype(queryResult.getPaymentType());
                orderList.setTradeno(queryResult.getTradeNo());
                orderList.setPaytime(queryResult.getPayTime());
                
                //利用資料庫介面將更新後的資料儲存到資料庫
                orderListRepository.save(orderList);

            /**
             * 0=未付款
             * 1=付款成功
             * 2=付款失敗
             * 3=取消付款
             * 6=退款
             */    
            // 如果付款成功（交易狀態為 "1"），更新商品庫存
                if ("0".equals(queryResult.getTradeStatus()) ||"1".equals(queryResult.getTradeStatus())) {
                    try {
                        Integer addresult = productService.AddNum(orderList.getPid(), orderList.getNum());
                        log.info("付款成功，庫存更新結果：{}", addresult);
                    } catch (Exception e) {
                        log.error("更新商品 {} 庫存時發生錯誤：{}", orderList.getPid(), e.getMessage());
                    }
                } else if ("3".equals(queryResult.getTradeStatus()) || "6".equals(queryResult.getTradeStatus())) {
                    try {
                        Integer subresult = productService.SubNum(orderList.getPid(), orderList.getNum());
                        log.info("退款(取消)成功，庫存更新結果：{}", subresult);
                    } catch (Exception e) {
                        log.error("更新商品 {} 庫存時發生錯誤：{}", orderList.getPid(), e.getMessage());
                    }
                }
            }
            log.info("更新 {} 筆訂單明細，訂單編號：{}，新狀態：{}", 
                    orderLists.size(), queryResult.getMerchantOrderNo(), queryResult.getTradeStatus());
        } else {
            log.warn("找不到訂單明細：{}，或查詢交易資訊失敗，無法更新", queryResult.getMerchantOrderNo());
        }
    }
}
