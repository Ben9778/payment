package com.ylz.yx.pay.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 退款订单表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayTkdd00 {

    public static final String STATE_INIT = "0"; //订单生成
    public static final String STATE_ING = "1"; //退款中
    public static final String STATE_SUCCESS = "2"; //退款成功
    public static final String STATE_FAIL = "3"; //退款失败
    public static final String STATE_CLOSED = "4"; //退款任务关闭

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
     * 支付渠道
     */
    private String zfqd00;

    /**
     * 支付方式
     */
    private String zffs00;

    /**
     * 退款金额,单位分
     */
    private Long tkje00;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭
     */
    private String tkzt00;

    /**
     * 退款原因
     */
    private String tkyy00;

    /**
     * 订单创建时间
     */
    private Date ddcjsj;

    /**
     * 退款成功时间
     */
    private Date tkcgsj;

    /**
     * 渠道通知地址
     */
    private String ybtzdz;

    /**
     * 渠道通知状态, 0-未发送,  1-已发送
     */
    private String tzzt00;

    /**
     * 渠道参数（特定支付方式）
     */
    private String qdcs00;

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

    /**
     * 医院ID
     */
    private String yyid00;

}
