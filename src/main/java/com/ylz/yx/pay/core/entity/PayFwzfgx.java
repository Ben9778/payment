package com.ylz.yx.pay.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 服务渠道与支付渠道关系表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayFwzfgx implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 服务渠道id
     */
    private String fwqdid;

    /**
     * 支付渠道id
     */
    private String zfqdid;

    /**
     * 支付方式["ali_bar", "ali_qr"]
     */
    private String zffs00;

}
