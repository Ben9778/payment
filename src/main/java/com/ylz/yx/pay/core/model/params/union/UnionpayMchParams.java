package com.ylz.yx.pay.core.model.params.union;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import com.ylz.yx.pay.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 银联 普通商户参数定义
 */
@Data
public class UnionpayMchParams extends NormalMchParams {

    /** 网关地址 */
    private String gatewayUrl;

    /** 对账文件地址 */
    private String fileUrl;

    /** 商户编号 **/
    private String merId;

    /** 商户私钥证书 **/
    private String privateCertFile;

    /** 商户私钥证书密码 **/
    private String privateCertPwd;

    /** 商户私钥证书类型 **/
    private String privateCertType = "PKCS12";

    /** 敏感信息加密证书 **/
    private String encryptCert;

    /** 中级证书 **/
    private String middleCert;

    /** 根证书 **/
    private String rootCert;

    /** 报文版本号 **/
    private String version;

    /** 签名方式，证书方式固定01 **/
    private String signMethod = "01";

    /** 是否验证验签证书的CN，测试环境请设置false，生产环境请设置true。非false的值默认都当true处理 **/
    private boolean ifValidateCNName = false;

    /** 是否验证https证书，测试环境请设置false，生产环境建议优先尝试true，不行再false。非true的值默认都当false处理 **/
    private boolean ifValidateRemoteCert = false;

    /** 商户公钥证书 **/
    private String publicCertFile;

    @Override
    public String deSenData() {
        UnionpayMchParams isvParams = this;
        if (StringUtils.isNotBlank(this.privateCertPwd)) {
            isvParams.setPrivateCertPwd(StringKit.str2Star(this.privateCertPwd, 0, 3, 6));
        }
        return ((JSONObject) JSON.toJSON(isvParams)).toJSONString();
    }
}
