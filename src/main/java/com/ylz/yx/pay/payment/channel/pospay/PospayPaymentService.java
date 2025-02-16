package com.ylz.yx.pay.payment.channel.pospay;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.AbstractPaymentService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.util.PaywayUtil;
import org.springframework.stereotype.Service;

/**
 * 银联POS机下单
 */
@Service
public class PospayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.POSPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return PaywayUtil.getRealPaywayService(this, payZfdd00.getZffs00()).preCheck(rq, payZfdd00);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        return PaywayUtil.getRealPaywayService(this, payZfdd00.getZffs00()).pay(rq, payZfdd00, mchAppConfigContext);
    }

}
