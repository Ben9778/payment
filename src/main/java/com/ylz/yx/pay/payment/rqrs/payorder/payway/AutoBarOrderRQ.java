package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/*
 * 支付方式： AUTO_BAR
 */
@Data
public class AutoBarOrderRQ extends UnifiedOrderRQ {

    /** 条码值 **/
    private String authCode;

    /** 客户端IP地址 **/
    private String clientIp;

    /** 构造函数 **/
    public AutoBarOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.AUTO_BAR);
    }

}
