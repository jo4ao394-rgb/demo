package com.example.demo.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 資料加密服務
 * 提供敏感資料的加密和解密功能
 * 
 * 功能包括：
 * - AES-GCM 加密/解密
 * - Base64 編碼處理
 * - 安全的金鑰管理
 * - IV（初始向量）生成
 */
@Slf4j
@Service
public class DataEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${app.encryption.key:myDefaultEncryptionKey123456789012}")
    private String encryptionKey;

    /**
     * 加密敏感資料
     * @param plainText 明文
     * @return 加密後的 Base64 字符串
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // 生成隨機 IV
            byte[] iv = generateIV();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] encryptedText = cipher.doFinal(plainText.getBytes());
            
            // 將 IV 和加密資料組合
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedText.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedText, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedText.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            log.error("資料加密失敗: {}", e.getMessage());
            throw new RuntimeException("資料加密失敗", e);
        }
    }

    /**
     * 解密敏感資料
     * @param encryptedText 加密的 Base64 字符串
     * @return 解密後的明文
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // 解碼 Base64
            byte[] decodedText = Base64.getDecoder().decode(encryptedText);
            
            // 提取 IV 和加密資料
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decodedText, 0, iv, 0, GCM_IV_LENGTH);
            
            byte[] encrypted = new byte[decodedText.length - GCM_IV_LENGTH];
            System.arraycopy(decodedText, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] decryptedText = cipher.doFinal(encrypted);
            
            return new String(decryptedText);
            
        } catch (Exception e) {
            log.error("資料解密失敗: {}", e.getMessage());
            throw new RuntimeException("資料解密失敗", e);
        }
    }

    /**
     * 加密信用卡號碼（只顯示後四位）
     * @param cardNumber 信用卡號碼
     * @return 遮罩後的信用卡號碼和加密資料
     */
    public String maskAndEncryptCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        // 儲存加密的完整卡號（實際專案中應儲存在安全的地方）
        String encrypted = encrypt(cardNumber);
        
        // 返回遮罩版本用於顯示
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    /**
     * 生成安全的初始向量
     * @return IV 位元組陣列
     */
    private byte[] generateIV() {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            return iv;
        } catch (NoSuchAlgorithmException e) {
            // 如果 getInstanceStrong() 失敗，使用預設的 SecureRandom
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            return iv;
        }
    }

    /**
     * 獲取加密金鑰
     * @return SecretKey
     */
    private SecretKey getSecretKey() {
        // 確保金鑰長度為 32 字節（256 位）
        String key = encryptionKey;
        if (key.length() < 32) {
            key = String.format("%-32s", key).replace(' ', '0');
        } else if (key.length() > 32) {
            key = key.substring(0, 32);
        }
        
        byte[] keyBytes = key.getBytes();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 生成隨機加密金鑰（用於初始化）
     * @return Base64 編碼的金鑰
     */
    public static String generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // AES-256
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("無法生成加密金鑰", e);
        }
    }

    /**
     * 驗證字串是否為加密格式
     * @param text 待驗證的字串
     * @return 是否為加密格式
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            return decoded.length > GCM_IV_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 雜湊敏感資料（用於搜尋索引）
     * @param data 原始資料
     * @return SHA-256 雜湊值
     */
    public String hashForIndex(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("資料雜湊失敗: {}", e.getMessage());
            throw new RuntimeException("資料雜湊失敗", e);
        }
    }
}