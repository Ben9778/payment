package com.ylz.yx.pay.record.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Date;

@Data
@SuperBuilder
public class PayPosdzd {
    /**
     * 对账日期
     */
    private String dzrq00;

    /**
     * 商户号
     */
    private String shh000;

    /**
     * 清算日期
     */
    private String qsrq00;

    /**
     * 交易日期
     */
    private String jyrq00;

    /**
     * 交易时间
     */
    private String jysj00;

    /**
     * 终端号
     */
    private String zdh000;

    /**
     * 交易金额
     */
    private Double jyje00;

    /**
     * 清算金额
     */
    private Double qsje00;

    /**
     * 手续费
     */
    private Double sxf000;

    /**
     * 流水号
     */
    private String lsh000;

    /**
     * 交易类型
     */
    private String jylx00;

    /**
     * 卡号
     */
    private String kh0000;

    /**
     * 系统参考号
     */
    private String xtckh0;

    /**
     * 发卡行
     */
    private String fkh000;

    /**
     * 卡类型
     */
    private String klx000;

}