//2.接收藍新伺服器回傳的付款結果，當使用者付款後，藍新會呼叫你設的 Notify URL，把結果 POST 回來
package com.example.demo.newwebpay.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
//@JsonProperty 用來標註 JSON 屬性與 Java 欄位的對應關係
public class PaymentResponse {
    @JsonProperty("Status")
    private String status; //狀態碼(藍新金流回傳的主要狀態（通常 "SUCCESS" 或 "FAIL"）)
    @JsonProperty("Message")
    private String message; //狀態對應的描述文字，如「付款成功」或「交易失敗」

    //付款完成通知
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class NotifyResponse extends PaymentResponse {
        @JsonProperty("Result")
        private Result result;

        @Data
       //藍新伺服器在付款完成（或失敗）後，POST 回你伺服器的 Notify URL 所帶的資料格式
        public static class Result {
            @JsonProperty("MerchantID")
            private String merchantID; //商店代號

            @JsonProperty("Amt")
            private int amt; //訂單金額

            @JsonProperty("TradeNo")
            private String tradeNo; //藍新金流交易序號

            @JsonProperty("MerchantOrderNo")
            private String merchantOrderNo; //商店訂單編號

            @JsonProperty("RespondType")
            private String respondType; //回傳格式

            @JsonProperty("IP")
            private String ip; //付款人 IP

            @JsonProperty("EscrowBank")
            private String escrowBank; //託管銀行代碼

            @JsonProperty("PaymentType")
            private String paymentType; //付款方式

            @JsonProperty("PayTime")
            private String payTime; //付款時間

            @JsonProperty("PayerAccount5Code")
            private String payerAccount5Code; //付款人帳號後五碼

            @JsonProperty("PayBankCode")
            private String payBankCode; //付款銀行代碼
        }

        //後端 Log 印出交易結果
        public String toString() {
            return "Status="+super.getStatus()+", Message="+super.getMessage()
                    +", MerchantID="+ getResult().merchantID+", Amt="+getResult().amt
                    +", TradeNo="+getResult().tradeNo+", MerchantOrderNo="+getResult().merchantOrderNo
                    +", RespondType="+getResult().respondType+", IP="+getResult().ip
                    +", EscrowBank="+getResult().escrowBank+", PaymentType="+getResult().paymentType
                    +", PayTime="+getResult().payTime+", PayerAccount5Code="+getResult().payerAccount5Code
                    +", PayBankCode="+getResult().payBankCode;
        }
    }

    //退款或請款結果
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CloseTradeResponse extends PaymentResponse {
        @JsonProperty("Result")
        private Result result;

        @Data
        public static class Result {
            @JsonProperty("MerchantID")
            private String merchantID;
            @JsonProperty("Amt")
            private int amt;
            @JsonProperty("TradeNo")
            private String tradeNo;
            @JsonProperty("MerchantOrderNo")
            private String merchantOrderNo;
        }
    }

    //查詢交易結果
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryTradeInfoResponse extends PaymentResponse {
        @JsonProperty("Result")
        private Result result;

        @Data
        // 嵌套的 Result 類
        public static class Result {
            @JsonProperty("MerchantID")
            private String merchantID;
            @JsonProperty("Amt")
            private int amt;
            @JsonProperty("TradeNo")
            private String tradeNo;
            @JsonProperty("MerchantOrderNo")
            private String merchantOrderNo;
            /**
             * 0=未付款
             * 1=付款成功
             * 2=付款失敗
             * 3=取消付款
             * 6=退款
             */
            @JsonProperty("TradeStatus")
            private String tradeStatus;
            /**
             * CREDIT=信用卡付款
             * VACC=銀行 ATM 轉帳付款
             * WEBATM=網路銀行轉帳付款
             * BARCODE=超商條碼繳費
             * CVS=超商代碼繳費
             * LINEPAY=LINE Pay 付款
             * ESUNWALLET=玉山 Wallet
             * TAIWANPAY=台灣 Pay
             * CVSCOM = 超商取貨付款
             * FULA=Fula 付啦
             */
            @JsonProperty("PaymentType")
            private String paymentType;
            @JsonProperty("CreateTime")
            private String createTime;
            @JsonProperty("PayTime")
            private String payTime;
            @JsonProperty("CheckCode")
            private String checkCode;
            /**
             * 預計撥款的時間
             * 回傳格式為：2014-06-25
             */
            @JsonProperty("FundTime")
            private String fundTime;
            /**
             * 付款資訊
             */
            @JsonProperty("PayInfo")
            private String payInfo;
            /**
             * 繳費有效期限
             * 格式為 Y-m-d H:i:s 例：2014-06-29 23:59:59
             */
            @JsonProperty("ExpireDate")
            private String expireDate;
            /**
             * 交易狀態
             * 0＝未付款
             * 1＝已付款
             * 2＝訂單失敗
             * 3＝訂單取消
             * 6＝已退款
             * 9＝付款中，待銀行確認
             */
            @JsonProperty("OrderStatus")
            private int orderStatus;
        }
    }
}