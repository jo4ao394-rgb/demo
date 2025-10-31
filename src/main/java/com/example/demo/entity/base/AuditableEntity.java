package com.example.demo.entity.base;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * 可審計的基礎實體類別
 * 所有需要審計功能的實體類別都應該繼承此類別
 * 
 * 功能包括：
 * - 自動記錄創建時間和最後修改時間
 * - 自動記錄創建者和最後修改者
 * - 版本控制（樂觀鎖）
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    /**
     * 創建時間
     */
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * 最後修改時間
     */
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    /**
     * 創建者
     */
    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    /**
     * 最後修改者
     */
    @LastModifiedBy
    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    /**
     * 版本號（樂觀鎖）
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 是否已刪除（軟刪除）
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * 刪除時間
     */
    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    /**
     * 刪除者
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    /**
     * 備註
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

    /**
     * 軟刪除方法
     * @param deletedBy 刪除者
     */
    public void softDelete(String deletedBy) {
        this.isDeleted = true;
        this.deletedDate = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 恢復軟刪除
     */
    public void undoSoftDelete() {
        this.isDeleted = false;
        this.deletedDate = null;
        this.deletedBy = null;
    }

    /**
     * 檢查是否為新實體
     */
    public boolean isNew() {
        return this.version == null || this.version == 0;
    }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}