package com.ylz.yx.pay.payment.service;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.impl.RefundOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
* 退款处理通用逻辑
*/
@Service
public class RefundOrderProcessService {

    @Autowired private RefundOrderService refundOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;

    /** 根据通道返回的状态，处理退款订单业务 **/
    public boolean handleRefundOrder4Channel(ChannelRetMsg channelRetMsg, PayTkdd00 payTkdd00){
        boolean updateOrderSuccess = true; //默认更新成功
        String refundOrderId = payTkdd00.getXtddh0();
        // 明确退款成功
        if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
            updateOrderSuccess = refundOrderService.updateIng2Success(refundOrderId, channelRetMsg.getChannelOrderId());
            if (updateOrderSuccess) {
                // 通知商户系统
                if(StringUtils.isNotEmpty(payTkdd00.getYbtzdz())){
                    payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
                }
            }
        //确认失败
        }else if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL){
            // 更新为失败状态
            updateOrderSuccess = refundOrderService.updateIng2Fail(refundOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
            // 通知商户系统
            if(StringUtils.isNotEmpty(payTkdd00.getYbtzdz())){
                payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
            }
        }
        return updateOrderSuccess;
    }

}
