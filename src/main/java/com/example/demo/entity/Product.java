//創建商品資料表(子類)
package com.example.demo.entity;

import java.io.Serializable;

//import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity //(實體類別)表示該類別的物件會對應到資料庫中的一個表格
@Data //可直接取用、設置欄位
@EqualsAndHashCode(callSuper = false) //解決繼承類別的 equals/hashCode 警告
@Table(name = "Productdata") //指定該實體類別對應的資料庫表格名稱

// @Embeddable 不能與 @Entity 同時使用，已移除
//Serializable序列化=把物件轉換成位元串(把物件轉換成可以儲存或傳輸的格式)
//implements 增加額外功能
public class Product extends BaseEntity implements Serializable{
    @Id //主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增長
    private Integer id;
    
    //基本欄位
    @Column(name = "Categoryid") //商品分類id
    private Integer categoryid;

    @Column(name = "Type") //類別
    private String type;

    @Column(name = "Title") //名稱
    private String title;

    @Column(name = "Price") //價格
    private Integer price;

    @Column(name = "Num") //數量
    private Integer num;

    @Lob //大型物件
    @Column(name = "Imagepath") //圖片
    @JsonIgnore //不要序列化到 JSON (避免錯誤)
    private byte[] imagepath; //儲存大型檔案

    @Column(name = "Status") //上架狀態
    private Integer status;

    @Column(name = "Priority") //優先順序
    private Integer priority;
}