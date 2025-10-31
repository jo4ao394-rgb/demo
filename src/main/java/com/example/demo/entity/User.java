//創建用戶資料表(子類)
package com.example.demo.entity;

import java.io.Serializable;

//import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity //(實體類別)表示該類別的物件會對應到資料庫中的一個表格
@Data //可直接取用、設置欄位
@EqualsAndHashCode(callSuper = false) //解決繼承類別的 equals/hashCode 警告
@Table(name = "Userdata") //指定該實體類別對應的資料庫表格名稱

// @Embeddable 不能與 @Entity 同時使用，已移除
//Serializable序列化=把物件轉換成位元串(把物件轉換成可以儲存或傳輸的格式)
//implements 增加額外功能
public class User extends BaseEntity implements Serializable{
    @Id //主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增長
    private Integer uid;
    
    //基本欄位
    @Column(name = "Username") //在資料表建立欄位
    private String username;

    @Column(name = "Password")
    private String password;

    @Column(name = "Salt")
    private String salt; //密碼加密亂碼
  
    @Column(name = "Gender")
    private int gender;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Email")
    private String email; 

    @Column(name = "Avatar")
    private String avatar; //頭像

    @Column(name = "Is_delete")
    private Integer isDelete;
}
