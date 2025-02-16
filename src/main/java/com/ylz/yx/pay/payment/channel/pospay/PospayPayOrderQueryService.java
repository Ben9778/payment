package com.ylz.yx.pay.payment.channel.pospay;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.IPayOrderQueryService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.springframework.stereotype.Service;

/**
 * 银联POS机查单
 */
@Service
public class PospayPayOrderQueryService implements IPayOrderQueryService {

    private static final Logger log = new Logger(PospayPayOrderQueryService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.POSPAY;
    }

    @Override
    public ChannelRetMsg query(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【银联POS机查询订单】";
        log.info(logPrefix+" reqParams=默认成功");
        return ChannelRetMsg.unknown();
    }

}
