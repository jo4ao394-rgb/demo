package com.example.demo.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定義使用者詳細資訊服務
 * 暫時使用記憶體中的使用者資料，實際專案中應連接資料庫
 * 
 * 功能包括：
 * - 根據使用者名稱載入使用者資訊
 * - 提供使用者權限和角色資訊
 * - 整合未來的使用者資料庫查詢
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("載入使用者詳細資訊: {}", username);
        
        // TODO: 這裡應該從資料庫查詢使用者資訊
        // 暫時使用硬編碼的測試使用者
        return createTestUser(username);
    }

    /**
     * 建立測試使用者（開發階段使用）
     * @param username 使用者名稱
     * @return UserDetails 物件
     */
    private UserDetails createTestUser(String username) {
        switch (username.toLowerCase()) {
            case "admin":
                return User.builder()
                        .username("admin")
                        .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6hsOzke8eW") // password: admin123
                        .authorities(getAdminAuthorities())
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
                        
            case "user":
                return User.builder()
                        .username("user")
                        .password("$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.") // password: password
                        .authorities(getUserAuthorities())
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
                        
            case "test@example.com":
                return User.builder()
                        .username("test@example.com")
                        .password("$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.") // password: password
                        .authorities(getUserAuthorities())
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
                        
            default:
                log.warn("找不到使用者: {}", username);
                throw new UsernameNotFoundException("使用者不存在: " + username);
        }
    }

    /**
     * 取得管理員權限
     * @return 權限列表
     */
    private List<SimpleGrantedAuthority> getAdminAuthorities() {
        return Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("READ_PRIVILEGE"),
                new SimpleGrantedAuthority("WRITE_PRIVILEGE"),
                new SimpleGrantedAuthority("DELETE_PRIVILEGE")
        );
    }

    /**
     * 取得一般使用者權限
     * @return 權限列表
     */
    private List<SimpleGrantedAuthority> getUserAuthorities() {
        return Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("READ_PRIVILEGE")
        );
    }
}