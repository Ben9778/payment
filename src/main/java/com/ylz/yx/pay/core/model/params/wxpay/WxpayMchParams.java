package com.ylz.yx.pay.core.model.params.wxpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import com.ylz.yx.pay.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 微信官方支付 配置参数
 */
@Data
public class WxpayMchParams extends NormalMchParams {

    /** 网关地址 */
    private String gatewayUrl;

    /**
     * 应用App ID
     */
    private String appId;

    /**
     * 应用AppSecret
     */
    private String appSecret;

    /**
     * 微信支付商户号
     */
    private String mchId;

    /**
     * 子商户ID
     */
    private String subMchId;

    /**
     * 子账户appID
     */
    private String subMchAppId;

    /**
     * oauth2地址
     */
    private String oauth2Url;

    /**
     * API密钥
     */
    private String key;

    /**
     * 微信支付API版本
     **/
    private String apiVersion = CS.PAY_IF_VERSION.WX_V2;

    /**
     * API V3秘钥
     **/
    private String apiV3Key;

    /**
     * 序列号
     **/
    private String serialNo;

    /**
     * API证书(.p12格式)
     **/
    private String cert;

    /** 证书文件(.pem格式) **/
    private String apiClientCert;

    /**
     * 私钥文件(.pem格式)
     **/
    private String apiClientKey;

    @Override
    public String deSenData() {
        WxpayMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.appSecret)) {
            mchParams.setAppSecret(StringKit.str2Star(this.appSecret, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.key)) {
            mchParams.setKey(StringKit.str2Star(this.key, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.apiV3Key)) {
            mchParams.setApiV3Key(StringKit.str2Star(this.apiV3Key, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.serialNo)) {
            mchParams.setSerialNo(StringKit.str2Star(this.serialNo, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
