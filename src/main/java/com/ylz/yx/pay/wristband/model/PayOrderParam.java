package com.ylz.yx.pay.wristband.model;

import lombok.Data;

@Data
public class PayOrderParam {

    /**
     * 腕带付-获取患者信息
     */
    private String zyid00;// 住院ID

    /**
     * 腕带付-下单
     */
    private String zfje00;// 支付金额，单位元

    private String brxm00;// 病人姓名

    private String zyh000;// 住院号

    /**
     * 电子病历-获取待结算信息
     */
    private String brid00;// 病人ID

    private String ghh000;// 挂号号

    private String kh0000;// 卡号

    private String xtgzh0;// 系统跟踪号

    private String ssxt00;// 所属系统

}
