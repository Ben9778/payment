package com.ylz.yx.pay.payment.channel;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRQ;

/*
* 调起上游渠道侧退款接口
*/
public interface IRefundService {

    /** 获取到接口code **/
    String getIfCode();

    /** 前置检查如参数等信息是否符合要求， 返回错误信息或直接抛出异常即可  */
    String preCheck(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00);

    /** 调起退款接口，并响应数据；  内部处理普通商户和服务商模式  **/
    ChannelRetMsg refund(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception;

    /** 退款查单接口  **/
    ChannelRetMsg query(PayTkdd00 payTkdd00, MchAppConfigContext mchAppConfigContext) throws Exception;

}
