package com.ylz.yx.pay.record.model;

import lombok.Data;

import java.util.Date;

@Data
public class PayDzhzb0 {
    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 系统对账时间
     */
    private Date xtdzsj;

    /**
     * 平台金额
     */
    private Long ptje00;

    /**
     * 平台笔数
     */
    private Long ptbs00;

    /**
     * 服务渠道金额
     */
    private Long fwqdje;

    /**
     * 服务渠道笔数
     */
    private Long fwqdbs;

    /**
     * 支付渠道金额
     */
    private Long zfptje;

    /**
     * 支付渠道笔数
     */
    private Long zfptbs;

}