package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： UNION_QR
 */
@Data
public class UnionQrOrderRQ extends CommonPayDataRQ {

    /** 构造函数 **/
    public UnionQrOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.UNION_QR);
    }

}
