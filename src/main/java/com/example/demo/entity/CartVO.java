//創建購物車結帳資料表(子類) 暫時性物件：查詢完就消失，不持久化
package com.example.demo.entity;

import java.io.Serializable;

//import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.Data;
//import lombok.EqualsAndHashCode;

@Entity //(實體類別)表示該類別的物件會對應到資料庫中的一個表格
@Data //可直接取用、設置欄位
//@EqualsAndHashCode(callSuper = false) //解決繼承類別的 equals/hashCode 警告
@Table(name = "Cartvodata") //指定該實體類別對應的資料庫表格名稱

// @Embeddable 不能與 @Entity 同時使用，已移除
//Serializable序列化=把物件轉換成位元串(把物件轉換成可以儲存或傳輸的格式)
//implements 增加額外功能
public class CartVO implements Serializable{
    @Id //主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增長
    private Integer cid;
    
    //基本欄位
    @Column(name = "Uid") //使用者id
    private Integer uid;

    @Column(name = "Pid") //商品id
    private Integer pid;

    @Column(name = "Num") //數量
    private Integer num;

    @Column(name = "Price") //價格
    private Integer price;

    @Column(name = "Title") //名稱
    private String title;
    
    @Column(name = "RealPrice") //實際價格
    private Integer realPrice;
}
