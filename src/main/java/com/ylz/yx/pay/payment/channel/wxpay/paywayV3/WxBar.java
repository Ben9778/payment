package com.ylz.yx.pay.payment.channel.wxpay.paywayV3;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.wxpay.WxpayPaymentService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * 微信 条码支付
 */
@Service("wxpayPaymentByBarV3Service") //Service Name需保持全局唯一性
public class WxBar extends WxpayPaymentService {

    @Autowired
    private com.ylz.yx.pay.payment.channel.wxpay.payway.WxBar wxBar;

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return wxBar.preCheck(rq, payZfdd00);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        return wxBar.pay(rq, payZfdd00, mchAppConfigContext);
    }
}
