package com.ylz.yx.pay.payment.channel;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;

/**
* 查单（渠道侧）接口定义
*/
public interface IPayOrderQueryService {

    /** 获取到接口code **/
    String getIfCode();

    /** 查询订单 **/
    ChannelRetMsg query(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception;

}
