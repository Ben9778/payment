package com.ylz.yx.pay.system.channel.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 支付方式表 返回响应参数
 * </p>
 */
@Data
public class PayZfqd00RS implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 渠道名称
     */
    private String qdmc00;

    /**
     * 支付方式集合
     **/
    private List<PayZffs00RS> zffss0;

}
