# API 安全性架構實作指南

## 📋 安全架構概覽

基於您提供的四層安全架構，本專案已整合以下安全性套件：

| 層級 | 主要套件 | 功能 | 實作狀態 |
|------|----------|------|----------|
| **API 驗證層** | Spring Security + JWT + OAuth2 | 身分與授權 | ✅ 已配置 |
| **流量防禦層** | Bucket4j + Redis + WAF | 限流、防濫用 | ✅ 已配置 |
| **監控偵測層** | Actuator + Prometheus + Drools | 異常偵測、告警 | ✅ 已配置 |
| **資料安全層** | JPA + BCrypt + Vault | 資料加密與稽核 | ✅ 已配置 |

## 🛡️ 第一層：API 驗證層

### 1.1 Spring Security 基礎設定
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }
}
```

### 1.2 JWT 設定
- **jjwt-api**: JWT 介面定義
- **jjwt-impl**: JWT 實作
- **jjwt-jackson**: JSON 序列化支援

### 1.3 OAuth2 Resource Server
- 支援 JWT Token 驗證
- 整合 Spring Security

## 🚦 第二層：流量防禦層

### 2.1 Redis 整合
```properties
# Redis 設定
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000ms
spring.redis.lettuce.pool.max-active=8
```

### 2.2 Bucket4j 限流設定
```java
@Component
public class RateLimitService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public Bucket createBucket(String key, long capacity, Duration refill) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, refill));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
```

### 2.3 防護措施
- **OWASP Encoder**: XSS 防護
- **Spring Validation**: 輸入驗證
- **Commons Codec**: HMAC 驗證

## 📊 第三層：監控偵測層

### 3.1 Actuator 監控端點
```properties
# Actuator 設定
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

### 3.2 Drools 規則引擎
```java
@Service
public class AnomalyDetectionService {
    
    private KieContainer kieContainer;
    
    public void detectAnomalies(TransactionEvent event) {
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(event);
        kieSession.fireAllRules();
        kieSession.dispose();
    }
}
```

### 3.3 Prometheus 指標收集
- 自動收集 JVM 指標
- 自定義業務指標
- HTTP 請求指標

## 🔐 第四層：資料安全層

### 4.1 JPA 審計配置
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AuditableEntity {
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String lastModifiedBy;
}
```

### 4.2 BCrypt 密碼加密
```java
@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

### 4.3 Jasypt 設定檔加密
```properties
# application.properties
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD}

# 加密的設定值
database.password=ENC(encrypted_password_here)
```

### 4.4 HashiCorp Vault 整合
```java
@Configuration
public class VaultConfig {
    
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint vaultEndpoint = new VaultEndpoint();
        vaultEndpoint.setHost("localhost");
        vaultEndpoint.setPort(8200);
        vaultEndpoint.setScheme("http");
        
        return new VaultTemplate(vaultEndpoint, 
            new TokenAuthentication("your-vault-token"));
    }
}
```

## 🚀 實作步驟建議

### 階段 1：基礎安全設定
1. 設定 Spring Security 基本配置
2. 實作 JWT 認證機制
3. 配置 CORS 和 CSRF 防護

### 階段 2：進階防護
1. 整合 Redis 和限流機制
2. 設定輸入驗證和 XSS 防護
3. 實作 API 金鑰驗證

### 階段 3：監控和偵測
1. 配置 Actuator 監控端點
2. 設定 Prometheus 指標收集
3. 實作 Drools 異常偵測規則

### 階段 4：資料安全
1. 設定 JPA 審計功能
2. 實作敏感資料加密
3. 整合 Vault 金鑰管理

## 📝 設定檔範例

### application.yml
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-server
  
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
  
  datasource:
    url: jdbc:mysql://localhost:3306/secure_db
    username: ${DB_USERNAME}
    password: ENC(${ENCRYPTED_DB_PASSWORD})
  
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

jasypt:
  encryptor:
    algorithm: PBEWITHHMACSHA512ANDAES_256
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
```

## 🔍 測試建議

### 安全性測試
1. **滲透測試**: 使用 OWASP ZAP 進行自動化安全掃描
2. **負載測試**: 驗證限流機制的有效性
3. **認證測試**: 測試 JWT 和 OAuth2 認證流程

### 監控測試
1. **異常偵測**: 模擬異常交易觸發 Drools 規則
2. **指標收集**: 驗證 Prometheus 指標正確性
3. **告警機制**: 測試異常事件通報功能

## 📚 相關文件

- [Spring Security 官方文件](https://spring.io/projects/spring-security)
- [Bucket4j 限流指南](https://bucket4j.com/)
- [Drools 規則引擎](https://www.drools.org/)
- [HashiCorp Vault](https://www.vaultproject.io/)
- [OWASP 安全指南](https://owasp.org/)

## 🎯 下一步行動

1. **立即執行**: `mvn clean install` 確認所有相依性正確安裝
2. **建立設定**: 根據上述範例建立安全設定類別
3. **環境準備**: 安裝並設定 Redis、Prometheus 等外部服務
4. **測試驗證**: 實作並測試各層安全功能

---

**注意事項**：
- 請確保所有敏感設定都使用環境變數或加密儲存
- 定期更新安全套件版本
- 實作完整的日誌記錄和監控機制
- 遵循最小權限原則設定使用者權限