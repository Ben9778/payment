package com.ylz.yx.pay.record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ylz.yx.pay.system.approval.model.PaySpbzb0;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PayDzmxb0 {
    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 审批记录表ID
     */
    private String jlbid0;

    /**
     * 系统对账日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date xtdzsj;

    /**
     * 允许操作
     */
    private String yxcz00;

    /**
     * 订单创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ddcjsj;

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
     * 订单类型（门诊、住院）
     */
    private String ddlx00;

    /**
     * 业务类型（充值、退款）
     */
    private String ywlx00;

    /**
     * 患者姓名
     */
    private String hzxm00;

    /**
     * 就诊卡号/住院号
     */
    private String khzyh0;

    /**
     * 服务渠道ID
     */
    private String fwqdid;

    /**
     * 支付渠道
     */
    private String zfqd00;

    /**
     * 平台金额
     */
    private Double ptje00;

    /**
     * 订单状态
     */
    private String ddzt00;

    /**
     * 服务渠道金额
     */
    private Double fwqdje;

    /**
     * 支付渠道金额
     */
    private Double zfptje;

    /**
     * 服务对账结果（1-一致、2-长款、3-短款、4-金额不一致）
     */
    private String fwdzjg;

    /**
     * 支付对账结果（1-一致、2-长款、3-短款、4-金额不一致）
     */
    private String zfdzjg;

    /**
     * 对账结果
     */
    private String dzjg00;

    /**
     * 对账结果说明
     */
    private String dzjgsm;

    /**
     * 服务差异金额
     */
    private Double fwcyje;

    /**
     * 支付差异金额
     */
    private Double zfcyje;

    /**
     * 是否异常状态（0-否、1-是）
     */
    private String yczt00;

    /**
     * 处理状态（0-未处理、1-已处理、2-无需处理、3-审批中）
     */
    private String clzt00;

    /**
     * 处理方式
     */
    private String clfs00;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date clsj00;

    /**
     * 处理人
     */
    private String clr000;

    /**
     * 备注
     */
    private String bz0000;

    /**
     * 创建时间
     */
    private Date cjsj00;

    /**
     * 是否显示审批按钮
     */
    private String sfxsan;

    /**
     * 是否显示发起申请退款按钮
     */
    private String sqtkan;

    /**
     * 当前步骤
     */
    private String dqbz00;

    /**
     * 剩余步骤数
     */
    private Integer sybzs0;

    /**
     * 流程步骤表
     */
    private List<PaySpbzb0> bzList;
}