package com.ylz.yx.pay.payment.channel.unionpay.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UnionHttpUtil {

    private final static Logger logger = LoggerFactory.getLogger(UnionHttpUtil.class);

    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 功能：后台交易提交请求报文并接收同步应答报文<br>
     * @param reqData 请求报文<br>
     * @param reqUrl  请求地址<br>
     * @return 应答http 200返回true ,其他false<br>
     */
    public static Map<String,String> post(
            Map<String, String> reqData, String reqUrl, boolean isIfValidateRemoteCert) {
        if(reqData == null || reqUrl == null) {
            logger.error("post err: null input");
            return null;
        }
        logger.info("请求银联地址：" + reqUrl + "，请求参数：" + reqData);
        if(reqUrl.startsWith("https://") && !isIfValidateRemoteCert) {
            reqUrl = "u" + reqUrl;
        }
        try{
            byte[] respBytes = HttpsUtil.post(reqUrl, SDKUtil.createLinkString(reqData, false, true, DEFAULT_CHARSET).getBytes(DEFAULT_CHARSET));
            if(respBytes == null) {
                logger.error("post失败");
                return null;
            }
            Map<String,String> result = SDKUtil.parseRespString(new String(respBytes, DEFAULT_CHARSET));
            logger.info("应答参数：" + result);
            return result;
        } catch (Exception e) {
            logger.error("post失败：" + e.getMessage(), e);
            return null;
        }
    }


}
