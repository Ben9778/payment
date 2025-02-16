package com.ylz.yx.pay.payment.service;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.wristband.WristbandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
* 订单处理通用逻辑
*/
@Service
public class PayOrderProcessService {

    @Autowired private PayMchNotifyService payMchNotifyService;

    @Autowired private WristbandService wristbandService;

    /** 明确成功的处理逻辑（除更新订单其他业务） **/
    public void confirmSuccess(PayZfdd00 payZfdd00, boolean autoRecharge, boolean autoSettle, boolean autoRechargeAndSettle){
        //设置订单状态
        payZfdd00.setDdzt00(PayZfdd00.STATE_SUCCESS);

        if(autoRecharge){ // 自动充值
            //支付订单充值
            wristbandService.payOrderRecharge(payZfdd00);
        }
        if(autoRechargeAndSettle){ // 自动充值+结算
            //支付订单充值+结算
            wristbandService.payOrderRechargeAndSettle(payZfdd00);
        }
        // 操作员为掌上医院时，调用充值+结算接口
        if("99996".equals(payZfdd00.getCzyid0())){ // 自动充值+结算
            //支付订单充值+结算
            wristbandService.payOrderRechargeAndSettle(payZfdd00);
        }

        //发送商户通知
        payMchNotifyService.payOrderNotify(payZfdd00);
    }

    /** 明确失败的处理逻辑（除更新订单其他业务） **/
    public void confirmFail(PayZfdd00 payZfdd00){
        //设置订单状态
        payZfdd00.setDdzt00(PayZfdd00.STATE_FAIL);

        //发送商户通知
        payMchNotifyService.payOrderNotify(payZfdd00);
    }
}
