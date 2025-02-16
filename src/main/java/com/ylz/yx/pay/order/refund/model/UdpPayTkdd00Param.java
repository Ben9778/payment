package com.ylz.yx.pay.order.refund.model;

import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 退款订单表
 * </p>
 */
@Data
public class UdpPayTkdd00Param {

    /**
     * 系统订单号
     */
    private String xtddh0;

    /**
     * 服务渠道订单号
     */
    private String fwddh0;

    /**
     * 支付渠道订单号
     */
    private String zfddh0;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭
     */
    private String tkzt00;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭
     */
    private String ytkzt0;

    /**
     * 退款成功时间
     */
    private Date tkcgsj;

    /**
     * 支付错误码
     */
    private String zfcwm0;

    /**
     * 支付错误描述
     */
    private String zfcwms;

}
