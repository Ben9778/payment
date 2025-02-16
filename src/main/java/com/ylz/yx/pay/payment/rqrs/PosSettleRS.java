package com.ylz.yx.pay.payment.rqrs;

import lombok.Data;

/*
* POS结算 响应参数
*/
@Data
public class PosSettleRS extends AbstractRS {

    /** 业务响应码 **/
    private Integer code;

    /** 业务响应信息 **/
    private String msg;

}
