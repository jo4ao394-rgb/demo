//建立用戶明細資料庫操作介面(有這個介面可直接操作資料庫)
package com.example.demo.repository;

import com.example.demo.entity.OrderList;

import java.util.List;
//import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//可在此自定義查詢條件
@Repository //能利用此註解操作資料庫
//interface代表介面(也就是這個介面繼承JpaRepository的規格，只要照規格走)
//JpaRepository<類, 類主鍵>
public interface OrderListRepository extends JpaRepository<OrderList, Integer> {
    List<OrderList> findAllByMerchantorderno(String merchantOrderNo);
}
