package com.ylz.yx.pay.payment.rqrs;

import lombok.Data;

/*
* 自助机订单同步 响应参数
*/
@Data
public class SelfSynchRS extends AbstractRS {

    /** 业务响应码 **/
    private Integer code;

    /** 业务响应信息 **/
    private String msg;

}
