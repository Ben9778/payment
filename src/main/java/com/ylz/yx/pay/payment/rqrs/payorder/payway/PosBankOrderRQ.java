package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/*
 * 支付方式： POS_BANK
 */
@Data
public class PosBankOrderRQ extends UnifiedOrderRQ {

    /** POS机IP地址 **/
    @NotBlank(message = "POS机IP地址不能为空")
    private String posIp;

    /** 构造函数 **/
    public PosBankOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.POS_BANK);
    }

}
