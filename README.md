## 項目簡介
醫院信息系統的支付模塊，主要提供醫院的收款、帳務等相關功能。該項目支持多種支付渠道，
包括：WeChat Pay、Ali Pay、中國銀聯、雲閃付等,支付方式包含:二維碼支付、pos支付、人臉支付、條碼支付、
聚合支付等。

## 項目主要結構
``` 
├── README.md   # 項目說明文件
├── pom.xml     # Maven文件
├── payment     # 支付模塊
│   ├── channel  # 支付渠道:wechatPay、alipay、unionpay、cloudpay
│   ├── controller  # 各種渠道支付Api
│   ├── rqrs    # 各種支付方式的請求參數和回調參數
|——record      # 帳務模塊，提供賬單查詢
```
## 項目技術環境
- JDK 1.8
- Spring Boot 2.1
- mybatis-plus 3.4.2
- Oracle 11g
- Redis 5.0.5
- ActiveMQ 5.15.11

## 反饋與聯繫
如有任何疑問或建議，請發送郵件至：Bennywong01@outlook.com。
