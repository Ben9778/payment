package com.ylz.yx.pay.payment.rqrs.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/*
 * 支付方式： ALI_FACE
 */
@Data
public class AliFaceOrderRQ extends UnifiedOrderRQ {

    /** 用户 支付条码 **/
    @NotBlank(message = "支付条码不能为空")
    private String authCode;

    /** 客户端IP地址 **/
    private String clientIp;

    /** 构造函数 **/
    public AliFaceOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.ALI_FACE); //默认 ali_face, 避免validate出现问题
    }

}
