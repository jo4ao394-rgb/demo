//1.付款請求，用來把訂單資料轉成藍新金流要求的格式，送出交易
package com.example.demo.newwebpay.bean;

import com.example.demo.newwebpay.NewWebUtil;
import lombok.Data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

//付款
@Data
public class PaymentRequest {
    //藍新金流要求的欄位，前7個是原始訂單資料，後面兩個是加密後要送出的資料
    private String merchantID;  // 商店代號
    private String respondType; // 回傳格式
    private String timeStamp;   // 時間戳記
    private String version;     // 串接程式版本
    private String merchantOrderNo;     // 商店訂單編號
    private int amt;    // 訂單金額
    private String itemDesc;    // 商品資訊
    private String notifyURL;   // 支付通知網址
    private String tradeInfo;   // 交易資料 AES 加密
    private String tradeSha;    // 交易資料 SHA256 加密

    // Constructor(建構子)
    //將讀取到的資料放入欄位，並進行加密
    public PaymentRequest(String merchantID, String hashKey, String hashIv, String merchantOrderNo, int amt, String itemDesc, String notifyURL) {
        this.merchantID = merchantID;
        this.respondType = "JSON";
        this.timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        this.version = "2.0";
        this.merchantOrderNo = merchantOrderNo;
        this.amt = amt;
        this.itemDesc = itemDesc;
        this.notifyURL = notifyURL;

        // Encrypt TradeInfo and TradeSha
        //generateTradeInfo()產生TradeInfo字串(key=value&key=value格式)
        //encryptAES進行AES加密
        this.tradeInfo = NewWebUtil.encryptAES(generateTradeInfo(), hashKey, hashIv);
        //根據加密後的TradeInfo生成檢查碼
        //generateSHA256進行SHA256加密
        this.tradeSha = NewWebUtil.generateSHA256(this.tradeInfo, hashKey, hashIv);
    }

    // 生成 TradeInfo 字串(一整條格式為key=value&key=value，再到上面加密)
    private String generateTradeInfo() {
        return "MerchantID=" + merchantID +
                "&RespondType=" + respondType +
                "&TimeStamp=" + timeStamp +
                "&Version=" + version +
                "&MerchantOrderNo=" + merchantOrderNo +
                "&Amt=" + amt +
                "&ItemDesc=" + itemDesc +
                "&NotifyURL=" + notifyURL;
    }

    // 請退款
    @Data
    public static class CloseTrade {
        private String merchantID;
        private int amt;
        private String merchantOrderNo;
        private String postData; //加密後的資料

        //把退款或請款的請求資料加密成藍新金流規定的格式
        public CloseTrade(String merchantID, String hashKey, String hashIv, String merchantOrderNo, int amt) {
            this.merchantID = merchantID;
            this.amt = amt;
            this.merchantOrderNo = merchantOrderNo;

            // 生成加密的 TradeInfo 和 PostData
            //String.valueOf(amt)將int轉成String
            //generateData1組出退款資料
            String data1 = generateData1(merchantOrderNo, String.valueOf(amt));
            //encryptAES進行AES加密
            this.postData = NewWebUtil.encryptAES(data1, hashKey, hashIv);
        }

        // 生成退款請求的資料字串
        private static String generateData1(String mon, String amt) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("RespondType", "JSON");
            params.put("Version", "1.1");
            params.put("Amt", amt);
            params.put("MerchantOrderNo", mon);
            params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("IndexType", "1");  // 1: 商店訂單編號, 2: 藍新金流交易序號
            params.put("CloseType", "1");  // 1: 請款, 2: 退款

            // 將參數轉換為 URL 格式的 key=value&key=value
            return params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
        }

        // 發送退款請求並取得回應
        public PaymentResponse.CloseTradeResponse request(String url) throws Exception {
            Map<String, String> params = new HashMap<>();
            params.put("MerchantID_", this.merchantID);
            params.put("PostData_", this.postData);
            String response = NewWebUtil.sendPostMethod(url, params);
            return NewWebUtil.convertJson(response, PaymentResponse.CloseTradeResponse.class);
        }

    }

    // 單筆交易查詢
    @Data
    public static class QueryTradeInfo {
        private String merchantID;
        private String version;
        private String respondType;
        private String checkValue;
        private String timeStamp;
        private String merchantOrderNo;
        private int amt;

        //將讀取到的資料放入欄位
        public QueryTradeInfo(String merchantID, String hashKey, String hashIv, String merchantOrderNo, int amt) {
            this.merchantID = merchantID;
            this.version = "1.3";
            this.respondType = "JSON";
            this.checkValue = NewWebUtil.generateCheckValue(String.valueOf(amt), merchantID, merchantOrderNo, hashKey, hashIv);
            this.timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            this.merchantOrderNo = merchantOrderNo;
            this.amt = amt;
        }

        // 生成查詢交易資料的參數字串
        private Map<String, String> generateData() {
            Map<String, String> params = new HashMap<>();
            params.put("MerchantID", merchantID);
            params.put("Version", version);
            params.put("RespondType", respondType);
            params.put("TimeStamp", timeStamp);
            params.put("MerchantOrderNo", merchantOrderNo);
            params.put("Amt", String.valueOf(amt));
            params.put("CheckValue", checkValue);
            return params;
        }

        // 發送查詢交易請求並取得回應
        public PaymentResponse.QueryTradeInfoResponse request(String url) throws Exception {
            String response = NewWebUtil.sendPostMethod(url, generateData());
            return NewWebUtil.convertJson(response, PaymentResponse.QueryTradeInfoResponse.class);
        }
    }
}