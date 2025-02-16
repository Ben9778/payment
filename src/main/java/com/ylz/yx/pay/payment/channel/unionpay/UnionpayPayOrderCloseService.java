package com.ylz.yx.pay.payment.channel.unionpay;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.IPayOrderCloseService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 银联 关闭订单接口实现类
 */
@Service
public class UnionpayPayOrderCloseService implements IPayOrderCloseService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.UNIONPAY;
    }

    @Autowired
    private UnionpayPaymentService unionpayPaymentService;

    @Override
    public ChannelRetMsg close(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        try {
            return ChannelRetMsg.confirmSuccess(null);  //关单成功
        }catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage()); // 关单失败
        }
    }

}
