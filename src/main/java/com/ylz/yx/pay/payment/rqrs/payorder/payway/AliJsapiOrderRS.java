package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRS;
import com.ylz.yx.pay.utils.JsonKit;
import lombok.Data;

/*
 * 支付方式： ALI_JSAPI
 */
@Data
public class AliJsapiOrderRS extends UnifiedOrderRS {

    /** 调起支付插件的支付宝订单号 **/
    private String alipayTradeNo;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.ALI_APP;
    }

    @Override
    public String buildPayData(){
        return JsonKit.newJson("alipayTradeNo", alipayTradeNo).toString();
    }

}
