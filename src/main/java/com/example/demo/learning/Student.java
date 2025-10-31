//創建資料表
package com.example.demo.learning;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.Data;

@Entity //(實體類別)表示該類別的物件會對應到資料庫中的一個表格
@Data //可直接取用、設置欄位
@Table(name = "Studentdata") //指定該實體類別對應的資料庫表格名稱
@EntityListeners(AuditingEntityListener.class) //啟用異動功能(用於自動新增創建者與時間的部分)
public class Student {
    @Id //主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增長
    private Integer id;
    
    //基本欄位
    @Column(name = "Mark")
    private int mark;

    @Column(name = "Name")
    private String name;
    
    //建立與更新時間
    @Column(name = "created_time", nullable = false)
    @CreatedDate
    private LocalDateTime createdTime; //如果改為private String createdBy可以代表創建者

    @Column(name = "updated_time", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedTime; //如果改為private String updatedBy可以代表更新者
}

