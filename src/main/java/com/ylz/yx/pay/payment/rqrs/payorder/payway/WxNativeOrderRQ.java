package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： WX_NATIVE
 */
@Data
public class WxNativeOrderRQ extends CommonPayDataRQ {

    /** 构造函数 **/
    public WxNativeOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.WX_NATIVE);
    }

}
