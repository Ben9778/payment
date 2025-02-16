package com.ylz.yx.pay.payment.rqrs.payorder.payway;


import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： ALI_APP
 */
@Data
public class AliAppOrderRS extends UnifiedOrderRS {

    private String payData;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.ALI_APP;
    }

    @Override
    public String buildPayData(){
        return payData;
    }

}
