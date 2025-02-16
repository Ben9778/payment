package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： ALI_PC
 */
@Data
public class AliPcOrderRQ extends CommonPayDataRQ {

    /** 构造函数 **/
    public AliPcOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.ALI_PC);
    }

}
