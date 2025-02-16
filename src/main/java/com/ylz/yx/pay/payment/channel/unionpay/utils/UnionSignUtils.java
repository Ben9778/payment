package com.ylz.yx.pay.payment.channel.unionpay.utils;

import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.File;
import java.security.PublicKey;
import java.util.Map;

import static com.ylz.yx.pay.payment.channel.unionpay.utils.SDKConstants.*;

/**
 * 银联接口签名工具类
 */
public class UnionSignUtils {

    private final static Logger logger = LoggerFactory.getLogger(UnionSignUtils.class);

    /**
     * 多证书签名(通过传入私钥证书路径和密码签名）<br>
     * 功能：如果有多个商户号接入银联,每个商户号对应不同的证书可以使用此方法:传入私钥证书和密码(并且在acp_sdk.properties中 配置 acpsdk.singleMode=false)<br>
     * @param reqData 请求报文map<br>
     * @param certPath 签名私钥文件（带路径）<br>
     * @param certPwd 签名私钥密码<br>
     * @param encoding 上送请求报文域encoding字段的值<br>
     * @return　签名后的map对象<br>
     */
    public static Map<String, String> signByCertInfo(Map<String, String> reqData, String certPath,
                                                     String certPwd, String encoding) {
        Map<String, String> data = SDKUtil.filterBlank(reqData);

        if (SDKUtil.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        if (SDKUtil.isEmpty(certPath) || SDKUtil.isEmpty(certPwd)) {
            logger.error("CertPath or CertPwd is empty");
            return data;
        }
        String signMethod = data.get(param_signMethod);
        if (SDKUtil.isEmpty(signMethod)) {
            signMethod = SIGNMETHOD_RSA;
        }

        String version = data.get(SDKConstants.param_version);
        try {
            if(VERSION_5_0_1.equals(version) || VERSION_5_0_0.equals(version)){
                if (SIGNMETHOD_RSA.equals(signMethod)) {
                    data.put(SDKConstants.param_certId, CertUtil.getCertIdByKeyStoreMap(certPath, certPwd));
                    data.put(SDKConstants.param_signature, SDKUtil.signRsa(data, certPath, certPwd, encoding));
                    return data;
                }
            } else if(VERSION_5_1_0.equals(version)){
                if (SIGNMETHOD_RSA.equals(signMethod)) {
                    data.put(SDKConstants.param_certId, CertUtil.getCertIdByKeyStoreMap(certPath, certPwd));
                    data.put(SDKConstants.param_signature, SDKUtil.signRsa2(data, certPath, certPwd, encoding));
                    return data;
                }
            }
            logger.error("未实现签名方法, version=" + version + ", signMethod=" + signMethod);
            return data;
        } catch (Exception e) {
            logger.error("Sign Error", e);
            return data;
        }
    }

    /**
     * 验证签名(SHA-1摘要算法)<br>
     * @param data 返回报文数据<br>
     * @param encoding 上送请求报文域encoding字段的值<br>
     * @return true 通过 false 未通过<br>
     */
    public static boolean validate(Map<String, String> data, String filepath, UnionpayMchParams unionpayMchParams, String encoding) {
        logger.info("验签处理开始");

        String middleCertPath = filepath + unionpayMchParams.getMiddleCert();
        String rootCertPath = filepath + unionpayMchParams.getMiddleCert();
        boolean isIfValidateCNName = unionpayMchParams.isIfValidateCNName();

        String signMethod = data.get(param_signMethod);
        if (SDKUtil.isEmpty(signMethod)) {
            signMethod = SIGNMETHOD_RSA;
        }

        String version = data.get(SDKConstants.param_version);

        try {
            if(SIGNMETHOD_RSA.equals(signMethod)) {
                String strCert = data.get(SDKConstants.param_signPubKeyCert);
                PublicKey verifyKey = null;
                if(!SDKUtil.isEmpty(strCert)){
                    verifyKey = CertUtil.verifyAndGetVerifyPubKey(strCert, middleCertPath, rootCertPath, isIfValidateCNName);
                }
                if(verifyKey == null) {
                    logger.error("未成功获取验签公钥，验签失败。");
                    return false;
                }
                if(VERSION_5_0_0.equals(version) || VERSION_5_0_1.equals(version) ) {
                    boolean result = SDKUtil.verifyRsa(data, verifyKey, encoding);
                    logger.info("验签" + (result? "成功":"失败") + "。");
                    return result;
                } else if(VERSION_5_1_0.equals(version)) {
                    boolean result = SDKUtil.verifyRsa2(data, verifyKey, encoding);
                    logger.info("验签" + (result? "成功":"失败") + "。");
                    return result;
                }
            }
            logger.error("无法判断验签方法，验签失败。version="+version+", signMethod="+signMethod);
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
