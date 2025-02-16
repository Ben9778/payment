package com.ylz.yx.pay.payment.rqrs.payorder;

import com.alibaba.fastjson.annotation.JSONField;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import lombok.Data;

/*
 * 关闭订单 响应参数
 */
@Data
public class ClosePayOrderRS extends AbstractRS {

    /** 上游渠道返回数据包 (无需JSON序列化) **/
    @JSONField(serialize = false)
    private ChannelRetMsg channelRetMsg;

}
