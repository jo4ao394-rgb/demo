//3.把 PaymentResponse 中的資料轉成你系統可以用的格式，用來解析、驗證回傳的加密內容（例如 AES 解密、檢查碼驗證）
package com.example.demo.newwebpay.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

//(改大小寫)
@Data
public class PayResponse {
    @JsonProperty("action")
    private String action; //告訴前端表單「要送到哪裡」
    @JsonProperty("merchantID")
    private String merchantID; //商店代號
    @JsonProperty("version")
    private String version; //串接程式版本
    @JsonProperty("tradeInfo")
    private String tradeInfo; //交易資料 AES 加密
    @JsonProperty("tradeSha")
    private String tradeSha; //交易資料 SHA256 加密

    //將 PaymentRequest 的資料轉成 PayResponse 格式，方便前端送出表單
    public static PayResponse of(PaymentRequest request, String action) {
        PayResponse response = new PayResponse();
        response.setAction(action);
        response.setMerchantID(request.getMerchantID());
        response.setVersion(request.getVersion());
        response.setTradeInfo(request.getTradeInfo());
        response.setTradeSha(request.getTradeSha());
        return response;
    }
}