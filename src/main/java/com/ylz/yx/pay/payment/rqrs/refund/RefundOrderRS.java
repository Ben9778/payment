package com.ylz.yx.pay.payment.rqrs.refund;

import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
* 退款订单 响应参数
*/
@Data
public class RefundOrderRS extends AbstractRS {

    /** 支付系统退款订单号 **/
    private String refundOrderId;

    /** 商户发起的退款订单号 **/
    private String mchRefundNo;

    /** 订单支付金额 **/
    private Long payAmount;

    /** 申请退款金额 **/
    private Long refundAmount;

    /** 退款状态 **/
    private String state;

    /** 渠道退款单号   **/
    private String channelOrderNo;

    /** 渠道返回错误代码 **/
    private String errCode;

    /** 渠道返回错误信息 **/
    private String errMsg;


    public static RefundOrderRS buildByRefundOrder(PayTkdd00 payTkdd00){

        if(payTkdd00 == null){
            return null;
        }

        RefundOrderRS result = new RefundOrderRS();
        BeanUtils.copyProperties(payTkdd00, result);
        result.setRefundOrderId(payTkdd00.getXtddh0());
        result.setMchRefundNo(payTkdd00.getFwddh0());
        result.setRefundAmount(payTkdd00.getTkje00());
        result.setState(payTkdd00.getTkzt00());
        result.setChannelOrderNo(payTkdd00.getZfddh0());
        result.setErrCode(payTkdd00.getZfcwm0());
        result.setErrMsg(payTkdd00.getZfcwms());
        return result;
    }


}
