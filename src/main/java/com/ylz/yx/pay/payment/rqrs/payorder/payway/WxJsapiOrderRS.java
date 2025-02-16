package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： WX_JSAPI
 */
@Data
public class WxJsapiOrderRS extends UnifiedOrderRS {

    /** 预支付数据包 **/
    private String payInfo;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.WX_APP;
    }

    @Override
    public String buildPayData(){
        return payInfo;
    }

}
