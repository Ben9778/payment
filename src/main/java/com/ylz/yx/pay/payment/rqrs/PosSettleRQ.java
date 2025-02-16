package com.ylz.yx.pay.payment.rqrs;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/*
* POS结算 请求参数对象
*/
@Data
public class PosSettleRQ extends AbstractMchAppRQ{

    /** POS机IP地址 **/
    @NotBlank(message = "POS机IP地址不能为空")
    private String posIp;

}
