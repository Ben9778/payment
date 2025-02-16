package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRS;
import com.ylz.yx.pay.utils.JsonKit;
import lombok.Data;

/*
 * 支付方式： YSF_JSAPI
 */
@Data
public class YsfJsapiOrderRS extends UnifiedOrderRS {

    /** 调起支付插件的云闪付订单号 **/
    private String redirectUrl;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.YSF_APP;
    }

    @Override
    public String buildPayData(){
        return JsonKit.newJson("redirectUrl", redirectUrl).toString();
    }

}
