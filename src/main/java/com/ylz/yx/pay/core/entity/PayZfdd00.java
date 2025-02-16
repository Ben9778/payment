package com.ylz.yx.pay.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 支付订单表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayZfdd00 implements Serializable {

    private static final long serialVersionUID=1L;

    public static final String STATE_INIT = "0"; //订单生成
    public static final String STATE_ING = "1"; //支付中
    public static final String STATE_SUCCESS = "2"; //支付成功
    public static final String STATE_FAIL = "3"; //支付失败
    public static final String STATE_CANCEL = "4"; //已撤销
    public static final String STATE_REFUND = "5"; //已退款
    public static final String STATE_CLOSED = "6"; //订单关闭

    public static final String REFUND_STATE_NONE = "0"; //未发生实际退款
    public static final String REFUND_STATE_SUB = "1"; //部分退款
    public static final String REFUND_STATE_ALL = "2"; //全额退款

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
     * 订单名称
     */
    private String ddmc00;

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
     * 订单创建时间
     */
    private Date ddcjsj;

    /**
     * 订单失效时间
     */
    private Date ddsxsj;

    /**
     * 订单支付时间
     */
    private Date ddzfsj;

    /**
     * 订单退款时间
     */
    private Date ddtksj;

    /**
     * 退款状态: 0-未发生实际退款, 1-部分退款, 2-全额退款
     */
    private String tkzt00;

    /**
     * 退款总金额,单位分
     */
    private Double tkzje0;

    /**
     * 渠道通知地址
     */
    private String ybtzdz;

    /**
     * 页面跳转地址
     */
    private String ymtzdz;

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
     * 渠道用户标识，如微信openId、支付宝账号
     */
    private String qdyhbs;

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

    /**
     * 审批状态
     */
    private String spzt00;

}
