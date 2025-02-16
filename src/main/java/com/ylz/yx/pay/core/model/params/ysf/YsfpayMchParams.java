package com.ylz.yx.pay.core.model.params.ysf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import com.ylz.yx.pay.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 云闪付 配置信息
 */
@Data
public class YsfpayMchParams extends NormalMchParams {

    /** 网关地址 */
    private String gatewayUrl;

    /** 商户编号 **/
    private String merId;

    /** serProvId **/
    private String serProvId;

    /** isvPrivateCertFile 证书 **/
    private String privateCertFile;

    /** isvPrivateCertPwd **/
    private String privateCertPwd;

    /** ysfpayPublicKey **/
    private String ysfpayPublicKey;

    /** acqOrgCodeList 支付机构号 **/
    private String acqOrgCode;

    @Override
    public String deSenData() {

        YsfpayMchParams isvParams = this;
        if (StringUtils.isNotBlank(this.privateCertPwd)) {
            isvParams.setPrivateCertPwd(StringKit.str2Star(this.privateCertPwd, 0, 3, 6));
        }
        if (StringUtils.isNotBlank(this.ysfpayPublicKey)) {
            isvParams.setYsfpayPublicKey(StringKit.str2Star(this.ysfpayPublicKey, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(isvParams)).toJSONString();
    }
}
