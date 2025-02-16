package com.ylz.yx.pay.payment.channel;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;

/*
* 调起上游渠道侧支付接口
*/
public interface IPaymentService {

    /** 获取到接口code **/
    String getIfCode();

    /** 是否支持该支付方式 */
    boolean isSupport(String wayCode);

    /** 前置检查如参数等信息是否符合要求， 返回错误信息或直接抛出异常即可  */
    String preCheck(UnifiedOrderRQ bizRQ, PayZfdd00 payZfdd00);

    /** 调起支付接口，并响应数据；  内部处理普通商户和服务商模式  **/
    AbstractRS pay(UnifiedOrderRQ bizRQ, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception;

}
