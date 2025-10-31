////創建父類ˋ
package com.example.demo.entity;

import java.time.LocalDateTime;
//import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass //父類別(使子類別能繼承父類別，父類別也連動到資料表欄位上)
@Data //可直接取用、設置欄位
public class BaseEntity {
    //建立與更新時間(人)
    @Column(name = "Created_by", nullable = false)
    @CreatedBy //創建者
    private String createdBy;

    @Column(name = "Created_time", nullable = false)
    @CreatedDate //創建時間
    private LocalDateTime createdTime;

    @Column(name = "Updated_by", nullable = false)
    @LastModifiedBy //更新者
    private String updatedBy;

    @Column(name = "Updated_time", nullable = false)
    @LastModifiedDate //更新時間
    private LocalDateTime updatedTime;
}
