package com.ylz.yx.pay.payment.rqrs.payorder;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
*  查询订单 响应参数
*/
@Data
public class QueryPayOrderRS extends AbstractRS {

    /**
     * 支付订单号
     */
    private String payOrderId;

    /**
     * 商户应用ID
     */
    private String appId;

    /**
     * 商户订单号
     */
    private String mchOrderNo;

    /**
     * 支付接口代码
     */
    private String ifCode;

    /**
     * 支付方式代码
     */
    private String wayCode;

    /**
     * 支付金额,单位分
     */
    private Long amount;

    /**
     * 支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭
     */
    private String state;

    /**
     * 商品标题
     */
    private String subject;

    /**
     * 渠道订单号
     */
    private String channelOrderNo;

    /**
     * 渠道支付错误码
     */
    private String errCode;

    /**
     * 渠道支付错误描述
     */
    private String errMsg;

    /**
     * 商户扩展参数
     */
    private String extParam;

    /**
     * 订单支付成功时间
     */
    private Long successTime;

    /**
     * 创建时间
     */
    private Long createdAt;


    public static QueryPayOrderRS buildByPayOrder(PayZfdd00 payZfdd00){

        if(payZfdd00 == null){
            return null;
        }

        QueryPayOrderRS result = new QueryPayOrderRS();
//        BeanUtils.copyProperties(payZfdd00, result);
        result.setPayOrderId(payZfdd00.getXtddh0());
        result.setAppId(payZfdd00.getFwqdid());
        result.setMchOrderNo(payZfdd00.getFwddh0());
        result.setIfCode(payZfdd00.getZfqd00());
        result.setWayCode(payZfdd00.getZffs00());
        result.setAmount(payZfdd00.getZfje00());
        result.setState(payZfdd00.getDdzt00());
        result.setSubject(payZfdd00.getDdmc00());
        result.setChannelOrderNo(payZfdd00.getZfddh0());
        result.setErrCode(payZfdd00.getZfcwm0());
        result.setErrMsg(payZfdd00.getZfcwms());
        result.setExtParam(payZfdd00.getKzcs00());
        result.setSuccessTime(payZfdd00.getDdzfsj() == null ? null : payZfdd00.getDdzfsj().getTime());
        result.setCreatedAt(payZfdd00.getDdcjsj() == null ? null : payZfdd00.getDdcjsj().getTime());

        return result;
    }


}
