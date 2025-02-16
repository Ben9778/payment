package com.ylz.yx.pay.record.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayMrdzhz {
    /**
     * 对账日期
     */
    private String dzrq00;

    /**
     * 订单类型（门诊、住院）
     */
    private String ddlx00;

    /**
     * 订单类型（门诊、住院）
     */
    private String ddlxmc;

    /**
     * 服务渠道ID
     */
    private String fwqdid;

    /**
     * 服务渠道
     */
    private String fwqdmc;

    /**
     * 支付渠道ID
     */
    private String zfqdid;

    /**
     * 支付渠道
     */
    private String zfqdmc;

    /**
     * 平台金额
     */
    private BigDecimal ptje00;

    /**
     * 平台笔数
     */
    private Long ptbs00;

    /**
     * 服务渠道金额
     */
    private BigDecimal fwqdje;

    /**
     * 服务渠道笔数
     */
    private Long fwqdbs;

    /**
     * 支付渠道金额
     */
    private BigDecimal zfptje;

    /**
     * 支付渠道笔数
     */
    private Long zfptbs;

    /**
     * 服务对账结果（1-一致、2-长款、3-短款、4-金额不一致）
     */
    private String fwdzjg;

    /**
     * 支付对账结果（1-一致、2-长款、3-短款、4-金额不一致）
     */
    private String zfdzjg;

    /**
     * 服务差异金额
     */
    private BigDecimal fwcyje;

    /**
     * 支付差异金额
     */
    private BigDecimal zfcyje;

    /**
     * 处理进度
     */
    private String cljd00;

}