//儲存藍新回傳資料表
package com.example.demo.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orderdata")
public class Order extends BaseEntity implements Serializable{
    
    @Id //主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增長
    @Column(name = "id")
    private Integer oid;

    //基本欄位
    @Column(name = "Uid")
    private Integer uid; //使用者id

    @Column(name = "Status")
    private String status; //狀態
    
    @Column(name = "Merchantorderno", unique = true, nullable = false, length = 30)
    private String merchantorderno; // 商店訂單編號
    
    @Column(name = "Totalamt", nullable = false, precision = 10, scale = 2)
    private Integer totalamt; // 訂單金額
    
    @Column(name = "Itemdescription", length = 500)
    private String itemdescription; // 商品描述
    
    @Column(name = "Tradestatus", nullable = false)
    private String tradestatus = "0"; // 訂單狀態 (0=未付款, 1=已付款, 2=訂單失敗, 3=訂單取消, 6=已退款)

    @Column(name = "Paymenttype", length = 20)
    private String paymenttype; // 付款方式
    
    @Column(name = "Tradeno", length = 30)
    private String tradeno; // 藍新金流交易序號
    
    @Column(name = "Paytime")
    private String paytime; // 付款時間
    
}