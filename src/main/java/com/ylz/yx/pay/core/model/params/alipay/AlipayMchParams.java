package com.ylz.yx.pay.core.model.params.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import com.ylz.yx.pay.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 支付宝 普通商户参数定义
 */
@Data
public class AlipayMchParams extends NormalMchParams {

    /** 网关地址 */
    private String gatewayUrl;

    /** 商户ID */
    private String mchId;

    /** 服务商ID */
    private String pid;

    /** 应用ID */
    private String appId;

    /** 应用私钥 */
    private String privateKey;

    /** 应用公钥 */
    private String publicKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 签名方式 **/
    private String signType;

    /** 异步通知地址 **/
    private String notifyUrl;

    /** 跳转通知地址 **/
    private String returnUrl;

    /** 字符编码格式 **/
    private String charset;

    @Override
    public String deSenData() {

        AlipayMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.privateKey)) {
            mchParams.setPrivateKey(StringKit.str2Star(this.privateKey, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.publicKey)) {
            mchParams.setPrivateKey(StringKit.str2Star(this.publicKey, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.alipayPublicKey)) {
            mchParams.setAlipayPublicKey(StringKit.str2Star(this.alipayPublicKey, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
