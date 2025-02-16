package com.ylz.yx.pay.payment.model;

import com.alipay.api.*;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.model.params.alipay.AlipayMchParams;
import com.ylz.yx.pay.payment.exception.ChannelException;
import lombok.AllArgsConstructor;
import lombok.Data;

 /*
 * 支付宝Client 包装类
 */
 @Data
 @AllArgsConstructor
 public class AlipayClientWrapper {

     private static final Logger log = new Logger(AlipayClientWrapper.class.getName());

     /** 缓存支付宝client 对象 **/
     private AlipayClient alipayClient;

     /** 封装支付宝接口调用函数 **/
     public <T extends AlipayResponse> T execute(AlipayRequest<T> request){

         try {
             T alipayResp = alipayClient.execute(request);
             return alipayResp;

         } catch (AlipayApiException e) { // 调起接口前出现异常，如私钥问题。  调起后出现验签异常等。

             log.error("调起支付宝execute[AlipayApiException]异常！", e);
             //如果数据返回出现验签异常，则需要抛出： UNKNOWN 异常。
             throw ChannelException.sysError(e.getMessage());

         } catch (Exception e) {
             log.error("调起支付宝execute[Exception]异常！", e);
             throw ChannelException.sysError("调用支付宝client服务异常");
         }
     }

     /*
      * 构建支付宝client 包装类
      */
     public static AlipayClientWrapper buildAlipayClientWrapper(AlipayMchParams alipayParams){

         AlipayClient alipayClient = null;
         alipayClient = new DefaultAlipayClient(alipayParams.getGatewayUrl(), alipayParams.getAppId(),
                 alipayParams.getPrivateKey(), "json", alipayParams.getCharset(),
                 alipayParams.getAlipayPublicKey(), alipayParams.getSignType());

         return new AlipayClientWrapper(alipayClient);
     }

































 }
