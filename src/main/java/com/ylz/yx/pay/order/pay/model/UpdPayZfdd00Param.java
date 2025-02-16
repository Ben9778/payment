package com.ylz.yx.pay.order.pay.model;

import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 支付订单表
 * </p>
 */
@Data
public class UpdPayZfdd00Param {

    /**
     * 系统订单号
     */
    private String xtddh0;

    /**
     * 支付渠道订单号
     */
    private String zfddh0;

    /**
     * 支付渠道
     */
    private String zfqd00;

    /**
     * 支付方式
     */
    private String zffs00;

    /**
     * 支付金额,单位分
     */
    private Long zfje00;

    /**
     * 支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭
     */
    private String ddzt00;

    /**
     * 支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭
     */
    private String yddzt0;

    /**
     * 订单支付时间
     */
    private Date ddzfsj;

    /**
     * 渠道通知状态, 0-未发送,  1-已发送
     */
    private String tzzt00;

    /**
     * 渠道用户标识，如微信openId、支付宝账号
     */
    private String qdyhbs;

    /**
     * 支付错误码
     */
    private String zfcwm0;

    /**
     * 支付错误描述
     */
    private String zfcwms;
}
