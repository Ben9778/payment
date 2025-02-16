package com.ylz.yx.pay.order.refund.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 退款订单表 响应参数
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayTkdd00RS implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 服务渠道ID
     */
    private String fwqdid;

    /**
     * 服务渠道名称
     */
    private String fwqdmc;

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
     * 原始系统订单号
     */
    private String yxtddh;

    /**
     * 原始服务渠道订单号
     */
    private String yfwddh;

    /**
     * 原始支付渠道订单号
     */
    private String yzfddh;

    /**
     * 患者姓名
     */
    private String hzxm00;

    /**
     * 患者手机号
     */
    private String hzsjh0;

    /**
     * 就诊卡号/住院号
     */
    private String khzyh0;

    /**
     * 患者身份证
     */
    private String hzsfzh;

    /**
     * 订单类型
     */
    private String ddlx00;

    /**
     * 订单类型名称
     */
    private String ddlxmc;

    /**
     * 订单名称
     */
    private String ddmc00;

    /**
     * 支付渠道
     */
    private String zfqd00;

    /**
     * 支付渠道名称
     */
    private String zfqdmc;

    /**
     * 支付方式
     */
    private String zffs00;

    /**
     * 支付方式名称
     */
    private String zffsmc;

    /**
     * 支付金额,单位分
     */
    private Double zfje00;

    /**
     * 退款金额,单位分
     */
    private Double tkje00;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭
     */
    private String tkzt00;

    /**
     * 退款状态名称
     */
    private String tkztmc;

    /**
     * 退款方式
     */
    private String tkfs00;

    /**
     * 退款原因
     */
    private String tkyy00;

    /**
     * 订单创建时间
     */
    private String ddcjsj;

    /**
     * 订单支付时间
     */
    private String ddzfsj;

    /**
     * 退款成功时间
     */
    private String tkcgsj;

    /**
     * 渠道通知地址
     */
    private String ybtzdz;

    /**
     * 渠道通知状态, 0-未发送,  1-已发送
     */
    private String tzzt00;

    /**
     * 扩展参数（渠道通知时原样返回）
     */
    private String kzcs00;

    /**
     * 操作员id
     */
    private String czyid0;

    /**
     * 操作员姓名
     */
    private String czyxm0;

    /**
     * 支付错误码
     */
    private String zfcwm0;

    /**
     * 支付错误描述
     */
    private String zfcwms;

}
