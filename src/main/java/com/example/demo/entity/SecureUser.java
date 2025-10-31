package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.entity.base.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 安全使用者實體
 * 整合審計功能和 Spring Security UserDetails
 * 
 * 功能包括：
 * - 自動審計創建和修改記錄
 * - 密碼安全儲存
 * - 賬戶狀態管理
 * - 角色和權限管理
 * - 敏感資料加密
 */
@Entity
@Table(name = "secure_users")
@Getter
@Setter
@NoArgsConstructor
public class SecureUser extends AuditableEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 使用者名稱（登入用）
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * 電子郵件
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * 加密後的密碼
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * 真實姓名（加密儲存）
     */
    @Column(name = "full_name_encrypted", length = 500)
    private String fullNameEncrypted;

    /**
     * 電話號碼（加密儲存）
     */
    @Column(name = "phone_encrypted", length = 500)
    private String phoneEncrypted;

    /**
     * 使用者角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    /**
     * 賬戶是否啟用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 賬戶是否未過期
     */
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    /**
     * 賬戶是否未鎖定
     */
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    /**
     * 憑證是否未過期
     */
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    /**
     * 最後登入時間
     */
    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    /**
     * 最後登入 IP
     */
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * 登入失敗次數
     */
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    /**
     * 賬戶鎖定時間
     */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 密碼最後更改時間
     */
    @Column(name = "password_changed_date")
    private LocalDateTime passwordChangedDate;

    /**
     * 兩步驟驗證是否啟用
     */
    @Column(name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    /**
     * 兩步驟驗證密鑰（加密儲存）
     */
    @Column(name = "two_factor_secret_encrypted", length = 500)
    private String twoFactorSecretEncrypted;

    // 建構函數
    public SecureUser(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordChangedDate = LocalDateTime.now();
    }

    // Spring Security UserDetails 實作
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired != null ? accountNonExpired : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
            return false;
        }
        return accountNonLocked != null ? accountNonLocked : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired != null ? credentialsNonExpired : true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null ? enabled : false;
    }

    // 業務方法
    
    /**
     * 記錄成功登入
     */
    public void recordSuccessfulLogin(String clientIP) {
        this.lastLoginDate = LocalDateTime.now();
        this.lastLoginIp = clientIP;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * 記錄登入失敗
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts != null ? this.failedLoginAttempts : 0) + 1;
        
        // 如果失敗次數達到 5 次，鎖定賬戶 30 分鐘
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
            this.accountNonLocked = false;
        }
    }

    /**
     * 解鎖賬戶
     */
    public void unlockAccount() {
        this.accountNonLocked = true;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    /**
     * 更新密碼
     */
    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedDate = LocalDateTime.now();
        this.credentialsNonExpired = true;
    }

    /**
     * 啟用兩步驟驗證
     */
    public void enableTwoFactor(String encryptedSecret) {
        this.twoFactorEnabled = true;
        this.twoFactorSecretEncrypted = encryptedSecret;
    }

    /**
     * 停用兩步驟驗證
     */
    public void disableTwoFactor() {
        this.twoFactorEnabled = false;
        this.twoFactorSecretEncrypted = null;
    }

    /**
     * 檢查密碼是否需要更新（90天規則）
     */
    public boolean isPasswordExpired() {
        if (passwordChangedDate == null) {
            return true;
        }
        return passwordChangedDate.isBefore(LocalDateTime.now().minusDays(90));
    }

    /**
     * 使用者角色枚舉
     */
    public enum UserRole {
        ADMIN,
        MANAGER,
        USER,
        GUEST
    }
}