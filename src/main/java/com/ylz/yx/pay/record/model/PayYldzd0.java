package com.ylz.yx.pay.record.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
public class PayYldzd0 {
    /**
     * 对账日期
     */
    private String dzrq00;

    /**
     * 交易代码
     */
    private String jydm00;

    /**
     * 代理机构标识码
     */
    private String dljgbs;

    /**
     * 发送机构标识码
     */
    private String fsjgbs;

    /**
     * 系统跟踪号
     */
    private String xtgzh0;

    /**
     * 交易传输时间
     */
    private String jycssj;

    /**
     * 账号
     */
    private String zh0000;

    /**
     * 交易金额
     */
    private Long jyje00;

    /**
     * 商户类别
     */
    private String shlb00;

    /**
     * 终端类型
     */
    private String zdlx00;

    /**
     * 查询流水号
     */
    private String zfddh0;

    /**
     * 支付方式（旧）
     */
    private String zffsj0;

    /**
     * 商户订单号
     */
    private String xtddh0;

    /**
     * 支付卡类型
     */
    private String zfklx0;

    /**
     * 原系统跟踪号
     */
    private String yxtgzh;

    /**
     * 原交易日期时间
     */
    private String yjyrq0;

    /**
     * 商户手续费
     */
    private String shsxf0;

    /**
     * 结算金额
     */
    private String jsje00;

    /**
     * 支付方式
     */
    private String zffs00;

    /**
     * 集团商户代码
     */
    private String jtshdm;

    /**
     * 交易类型
     */
    private String jylx00;

    /**
     * 交易子类
     */
    private String jyzl00;

    /**
     * 业务类型
     */
    private String ywlx00;

    /**
     * 账号类型
     */
    private String zhlx00;

    /**
     * 账单类型
     */
    private String zdlx01;

    /**
     * 账单号码
     */
    private String zdhm00;

    /**
     * 交互方式
     */
    private String jhfs00;

    /**
     * 原查询流水号
     */
    private String yzfddh;

    /**
     * 商户代码
     */
    private String shdm00;

    /**
     * 分账入账方式
     */
    private String fzrzfs;

    /**
     * 二级商户代码
     */
    private String ejshdm;

    /**
     * 二级商户简称
     */
    private String ejshjc;

    /**
     * 二级商户分账入账金额
     */
    private String ejfzrz;

    /**
     * 清算净额
     */
    private String qsje00;

    /**
     * 终端号
     */
    private String zdh000;

    /**
     * 商户自定义域
     */
    private String shzdy0;

    /**
     * 优惠金额
     */
    private String yhje00;

    /**
     * 发票金额
     */
    private String fpje00;

    /**
     * 分期付款附加手续费
     */
    private String fqfksx;

    /**
     * 分期付款期数
     */
    private String fqfkqs;

    /**
     * 交易介质
     */
    private String jyjz00;

    /**
     * 原交易订单号
     */
    private String yxtddh;

    /**
     * 保留使用
     */
    private String blsy00;
}