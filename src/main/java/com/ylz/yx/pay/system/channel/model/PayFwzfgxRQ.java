package com.ylz.yx.pay.system.channel.model;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 服务渠道与支付渠道关系表
 * </p>
 */
@Data
public class PayFwzfgxRQ implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 支付渠道id
     */
    private String zfqdid;

    /**
     * 支付方式["ali_bar", "ali_qr"]
     */
    private String zffs00;

}
