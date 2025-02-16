package com.ylz.yx.pay.record.model;

import lombok.Data;
import java.util.Date;

@Data
public class PayWxdzd0 {
    /**
     * 对账日期
     */
    private String dzrq00;

    /**
     * 交易时间
     */
    private String jysj00;

    /**
     * 公众账号ID
     */
    private String gzzhid;

    /**
     * 商户号
     */
    private String shhid0;

    /**
     * 子商户号（特约）
     */
    private String zshhid;

    /**
     * 设备号
     */
    private String sbh000;

    /**
     * 支付渠道订单号
     */
    private String zfddh0;

    /**
     * 系统订单号
     */
    private String xtddh0;

    /**
     * 用户标识 openid
     */
    private String yhbs00;

    /**
     * 交易类型
     */
    private String jylx00;

    /**
     * 交易状态
     */
    private String jyzt00;

    /**
     * 付款银行
     */
    private String fkyh00;

    /**
     * 货币种类
     */
    private String hbzl00;

    /**
     * 总金额
     */
    private Double zje000;

    /**
     * 企业红包金额
     */
    private Double qyhbje;

    /**
     * 微信退款单号
     */
    private String zftkdh;

    /**
     * 商户退款单号
     */
    private String xttkdh;

    /**
     * 退款金额
     */
    private Double tkje00;

    /**
     * 企业红包退款金额
     */
    private Double qyhbtk;

    /**
     * 退款类型
     */
    private String tklx00;

    /**
     * 退款状态
     */
    private String tkzt00;

    /**
     * 订单名称
     */
    private String ddmc00;

    /**
     * 商户数据包
     */
    private String shsjb0;

    /**
     * 手续费
     */
    private Double sxf000;

    /**
     * 费率
     */
    private Double fl0000;

    /**
     * 订单金额
     */
    private Double ddje00;

    /**
     * 申请退款金额
     */
    private Double sqtkje;

    /**
     * 费率备注
     */
    private String flbz00;

    /**
     * 退款申请时间
     */
    private String tksqsj;

    /**
     * 退款成功时间
     */
    private String tkcgsj;

    /**
     * 创建时间
     */
    private Date cjsj00;
}