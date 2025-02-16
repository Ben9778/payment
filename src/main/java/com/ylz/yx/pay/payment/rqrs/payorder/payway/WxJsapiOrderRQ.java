package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import org.hibernate.validator.constraints.NotBlank;

/*
 * 支付方式： WX_JSAPI
 */
@Data
public class WxJsapiOrderRQ extends UnifiedOrderRQ {

    /** 微信openid **/
    @NotBlank(message = "openid不能为空")
    private String openid;

    /** 构造函数 **/
    public WxJsapiOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.WX_JSAPI);
    }

    @Override
    public String getChannelUserId() {
        return this.openid;
    }
}
