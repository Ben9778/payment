package com.ylz.yx.pay.payment.rqrs.payorder;

import com.ylz.yx.pay.payment.rqrs.AbstractMchAppRQ;
import lombok.Data;

/*
* 查询订单请求参数对象
*/
@Data
public class QueryPayOrderRQ extends AbstractMchAppRQ {

    /** 商户订单号 **/
    private String mchOrderNo;

    /** 支付系统订单号 **/
    private String payOrderId;

}
