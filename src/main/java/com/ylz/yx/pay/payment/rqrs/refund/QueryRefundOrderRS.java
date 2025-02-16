package com.ylz.yx.pay.payment.rqrs.refund;

import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
* 查询退款单 响应参数
*/
@Data
public class QueryRefundOrderRS extends AbstractRS {

    /**
     * 退款订单号（支付系统生成订单号）
     */
    private String refundOrderId;

    /**
     * 支付订单号（与t_pay_order对应）
     */
    private String payOrderId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户退款单号（商户系统的订单号）
     */
    private String mchRefundNo;

    /**
     * 支付金额,单位分
     */
    private Long payAmount;

    /**
     * 退款金额,单位分
     */
    private Long refundAmount;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
     */
    private String state;

    /**
     * 渠道订单号
     */
    private String channelOrderNo;

    /**
     * 渠道错误码
     */
    private String errCode;

    /**
     * 渠道错误描述
     */
    private String errMsg;

    /**
     * 扩展参数
     */
    private String extParam;

    /**
     * 订单退款成功时间
     */
    private Long successTime;

    /**
     * 创建时间
     */
    private Long createdAt;


    public static QueryRefundOrderRS buildByRefundOrder(PayTkdd00 payTkdd00){

        if(payTkdd00 == null){
            return null;
        }

        QueryRefundOrderRS result = new QueryRefundOrderRS();
        //BeanUtils.copyProperties(payTkdd00, result);
        result.setRefundOrderId(payTkdd00.getXtddh0());
        result.setPayOrderId(payTkdd00.getYxtddh());
        result.setAppId(payTkdd00.getFwqdid());
        result.setMchRefundNo(payTkdd00.getFwddh0());
        result.setRefundAmount(payTkdd00.getTkje00());
        result.setState(payTkdd00.getTkzt00());
        result.setChannelOrderNo(payTkdd00.getZfddh0());
        result.setErrCode(payTkdd00.getZfcwm0());
        result.setErrMsg(payTkdd00.getZfcwms());
        result.setExtParam(payTkdd00.getKzcs00());
        result.setSuccessTime(payTkdd00.getTkcgsj() == null ? null : payTkdd00.getTkcgsj().getTime());
        result.setCreatedAt(payTkdd00.getDdcjsj() == null ? null : payTkdd00.getDdcjsj().getTime());
        return result;
    }


}
