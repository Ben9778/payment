package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： AUTO_BAR
 */
@Data
public class AutoBarOrderRS extends UnifiedOrderRS {

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.NONE;
    }

    @Override
    public String buildPayData(){
        return "";
    }

}
