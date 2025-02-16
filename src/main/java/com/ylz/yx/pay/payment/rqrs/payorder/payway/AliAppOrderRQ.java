package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

/*
* 支付方式： ALI_APP
*/
@Data
public class AliAppOrderRQ extends UnifiedOrderRQ {

    /** 构造函数 **/
    public AliAppOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.ALI_APP); //默认 wayCode, 避免validate出现问题
    }

}
