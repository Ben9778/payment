package com.ylz.yx.pay.record.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
public class PayZfbdzd {
    /**
     * 对账日期
     */
    private String dzrq00;

    /**
     * 支付渠道订单号
     */
    private String zfddh0;

    /**
     * 系统订单号
     */
    private String xtddh0;

    /**
     * 业务类型
     */
    private String ywlx00;

    /**
     * 订单名称
     */
    private String ddmc00;

    /**
     * 订单创建时间
     */
    private String ddcjsj;

    /**
     * 订单支付时间
     */
    private String ddzfsj;

    /**
     * 门店编号
     */
    private String mdbh00;

    /**
     * 门店名称
     */
    private String mdmc00;

    /**
     * 操作员
     */
    private String czy000;

    /**
     * 终端号
     */
    private String zdh000;

    /**
     * 对方账户
     */
    private String dfzh00;

    /**
     * 支付金额(元)
     */
    private Double zfje00;

    /**
     * 商家实收(元)
     */
    private Double sjss00;

    /**
     * 支付宝红包(元)
     */
    private Double zfbhb0;

    /**
     * 集分宝(元)
     */
    private Double jfb000;

    /**
     * 支付宝优惠(元)
     */
    private Double zfbyh0;

    /**
     * 商家优惠(元)
     */
    private Double sjyh00;

    /**
     * 券核销金额(元)
     */
    private Double qhxje0;

    /**
     * 券名称
     */
    private String qmc000;

    /**
     * 商家红包消费金额(元)
     */
    private Double sjhbje;

    /**
     * 卡消费金额(元)
     */
    private Double kxfje0;

    /**
     * 退款批次号/请求号
     */
    private String tkddh0;

    /**
     * 服务费(元)
     */
    private Double fwf000;

    /**
     * 分润(元)
     */
    private Double fr0000;

    /**
     * 备注
     */
    private String bz0000;

    /**
     * 创建时间
     */
    private Date cjsj00;
}