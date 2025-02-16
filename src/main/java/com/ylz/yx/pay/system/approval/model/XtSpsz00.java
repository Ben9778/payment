package com.ylz.yx.pay.system.approval.model;

import lombok.Data;

@Data
public class XtSpsz00 {
    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 是否启用（0：否；1：是）
     */
    private String sfqy00;

    /**
     * 应用模块（0：全部；1：支付订单退款；2：长款异常退款）
     */
    private String yymk00;

    /**
     * 审批方式（0：一人审批；1：全员审批）
     */
    private String spfs00;

    /**
     * 是否通信短信（0：否；1：是）
     */
    private String sfdxtz;

    /**
     * 流程设置
     */
    private String lcsz00;

    /**
     * 提示文案
     */
    private String tswa00;

}