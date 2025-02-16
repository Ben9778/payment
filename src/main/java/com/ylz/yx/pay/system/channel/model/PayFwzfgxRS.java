package com.ylz.yx.pay.system.channel.model;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 服务渠道与支付渠道关系表
 * </p>
 */
@Data
public class PayFwzfgxRS implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 渠道编码
     */
    private String zfqdid;

    /**
     * 渠道名称
     */
    private String zfqdmc;

    /**
     * 支付方式["ali_bar", "ali_qr"]
     */
    private String zffs00;

}
