package com.ylz.yx.pay.wristband;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.wristband.model.PayOrderParam;

public interface WristbandService {

    Object getPatientInfo(PayOrderParam param);

    Object payOrder(PayOrderParam param);

    void payOrderRecharge(PayZfdd00 payZfdd00);

    Object getSettleInfo(PayOrderParam param);

    Object MZPayOrder(PayOrderParam param);

    void payOrderRechargeAndSettle(PayZfdd00 payZfdd00);
}
