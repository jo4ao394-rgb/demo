# Demo 專案 - E-commerce 系統整合 NewWebPay 金流

這是一個整合了電商功能和 NewWebPay 金流支付的 Spring Boot 專案。

## 專案特色

### 電商核心功能 (來自 demo-backup)
- 使用者註冊、登入、修改資料
- 商品瀏覽和管理
- 購物車功能
- 會員資料管理
- FreeMarker 模板引擎
- MySQL 資料庫整合
- Redis 快取支援

### 金流支付功能 (來自 NewWebPay_Demo)
- NewWebPay 藍新金流串接
- 支付請求處理
- 支付回調處理
- 支付測試頁面
- 支付工具類和 Bean

## 技術架構

- **後端框架**: Spring Boot 3.5.6
- **Java 版本**: Java 21
- **資料庫**: MySQL
- **快取**: Redis
- **模板引擎**: FreeMarker
- **建構工具**: Maven
- **金流服務**: NewWebPay (藍新金流)

## 專案結構

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/          # 設定檔 (攔截器、Web設定)
│   │   ├── controller/      # 控制器 (包含電商和支付)
│   │   ├── dao/            # 資料存取層
│   │   ├── entity/         # 實體類
│   │   ├── service/        # 服務層 (包含支付服務)
│   │   ├── newwebpay/      # NewWebPay 金流相關
│   │   │   ├── bean/       # 支付請求/回應物件
│   │   │   └── NewWebUtil.java # 金流工具類
│   │   └── util/           # 工具類
│   └── resources/
│       ├── static/         # 靜態資源 (CSS, JS, HTML)
│       │   ├── css/        # 樣式檔案
│       │   ├── js/         # JavaScript 檔案
│       │   └── *.html      # NewWebPay 測試頁面
│       └── templates/      # FreeMarker 模板
└── test/                   # 測試檔案
```

## 主要功能頁面

### 電商功能
- `/` - 首頁
- `/login` - 登入頁面
- `/register` - 註冊頁面
- `/products` - 商品頁面
- `/cart` - 購物車頁面
- `/user/profile` - 使用者資料修改

### 支付功能
- `PayIndex.html` - 支付首頁
- `PaymentTestCenter.html` - 支付測試中心
- `AutoPayTest.html` - 自動支付測試
- `CallbackMonitor.html` - 回調監控
- `TestSuccess.html` - 測試成功頁面

## 如何執行

1. **環境準備**
   - 確保已安裝 Java 21
   - 確保已安裝 MySQL 和 Redis
   - 確保已安裝 Maven

2. **資料庫設定**
   - 建立 MySQL 資料庫
   - 修改 `application.properties` 中的資料庫連線設定

3. **啟動應用程式**
   ```bash
   mvn spring-boot:run
   ```

4. **存取應用程式**
   - 電商功能: http://localhost:8080
   - 支付測試: http://localhost:8080/PayIndex.html

## 設定檔

主要設定在 `src/main/resources/application.properties`:
- 資料庫連線設定
- Redis 設定
- NewWebPay 金流設定

## 開發說明

此專案整合了兩個原始專案：
1. **demo-backup**: 提供電商核心功能作為主要基礎
2. **NewWebPay_Demo**: 提供金流支付功能作為輔助整合

合併過程中統一了套件結構為 `com.example.demo`，並確保所有功能可以協同運作。

## 注意事項

- NewWebPay 金流測試需要有效的商店設定
- Redis 服務需要正常運行
- MySQL 資料庫需要正確的權限設定