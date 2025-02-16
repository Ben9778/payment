package com.ylz.yx.pay.system.channel.model;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 支付方式表
 * </p>
 */
@Data
public class PayZffs00RS implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 支付方式
     */
    private String zffs00;

    /**
     * 支付方式名称
     */
    private String zffsmc;

}
