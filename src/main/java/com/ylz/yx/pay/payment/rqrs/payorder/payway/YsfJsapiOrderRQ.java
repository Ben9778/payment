package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/*
 * 支付方式： YSF_JSAPI
 */
@Data
public class YsfJsapiOrderRQ extends UnifiedOrderRQ {

    /** 客户端IP地址 **/
    @NotBlank(message = "客户端IP地址不能为空")
    private String clientIp;

    /** 构造函数 **/
    public YsfJsapiOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.YSF_JSAPI);
    }

}
