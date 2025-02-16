package com.ylz.yx.pay.core.entity;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
public class PayFwdzd0 {
    /**
     * 对账日期
     */
    private String dzrq00;

    /**
     * 服务渠道ID
     */
    private String fwqdid;

    /**
     * 服务渠道名称
     */
    private String fwqdmc;

    /**
     * 服务渠道订单号
     */
    private String fwddh0;

    /**
     * 系统订单号
     */
    private String xtddh0;

    /**
     * 业务类型（门诊、住院）
     */
    private String ywlx00;

    /**
     * 交易类型（充值、退款）
     */
    private String jylx00;

    /**
     * 订单名称
     */
    private String ddmc00;

    /**
     * 支付金额(元)
     */
    private String zfje00;

    /**
     * 交易时间
     */
    private String jysj00;

    /**
     * 患者姓名
     */
    private String hzxm00;

    /**
     * 就诊卡号/住院号
     */
    private String khzyh0;

    /**
     * 创建时间
     */
    private Date cjsj00;

}