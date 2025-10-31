package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 配置屬性類別
 * 將 application.yml 中的 JWT 設定映射到 Java 物件
 * 
 * 步驟說明：
 * 1. 使用 @ConfigurationProperties 註解綁定配置
 * 2. 設定前綴 "jwt" 對應 YAML 中的 jwt: 節點
 * 3. 使用 Lombok 自動生成 getter/setter
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 簽名密鑰
     * 對應 YAML: jwt.secret
     */
    private String secret = "mySecretKey12345678901234567890123456789012345678901234567890";

    /**
     * Access Token 過期時間（毫秒）
     * 對應 YAML: jwt.expiration
     * 預設：24小時
     */
    private Long expiration = 86400000L;

    /**
     * Refresh Token 過期時間（毫秒）
     * 對應 YAML: jwt.refresh-expiration
     * 預設：7天
     */
    private Long refreshExpiration = 604800000L;

    /**
     * Token 發行者
     */
    private String issuer = "secure-ecommerce-api";

    /**
     * 檢查密鑰是否有效
     * JWT 密鑰長度應該至少 32 字節（256 位）
     */
    public boolean isSecretValid() {
        return secret != null && secret.length() >= 32;
    }

    /**
     * 獲取標準化的密鑰
     * 確保密鑰長度為 32 字節
     */
    public String getStandardizedSecret() {
        if (secret == null) {
            throw new IllegalStateException("JWT secret 不能為空");
        }
        
        if (secret.length() < 32) {
            // 如果密鑰太短，用 0 填充到 32 字節
            return String.format("%-32s", secret).replace(' ', '0');
        } else if (secret.length() > 32) {
            // 如果密鑰太長，截取前 32 字節
            return secret.substring(0, 32);
        }
        
        return secret;
    }

    /**
     * 獲取 Access Token 過期時間（秒）
     */
    public long getExpirationInSeconds() {
        return expiration / 1000;
    }

    /**
     * 獲取 Refresh Token 過期時間（秒）
     */
    public long getRefreshExpirationInSeconds() {
        return refreshExpiration / 1000;
    }
}