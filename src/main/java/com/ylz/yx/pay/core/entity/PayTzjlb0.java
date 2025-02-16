package com.ylz.yx.pay.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 服务渠道通知记录表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayTzjlb0 implements Serializable {

    //订单类型:1-支付,2-退款
    public static final int TYPE_PAY_ORDER = 1;
    public static final int TYPE_REFUND_ORDER = 2;

    //通知状态
    public static final int STATE_ING = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAIL = 3;

    private static final long serialVersionUID=1L;

    /**
     * 商户通知记录ID
     */
    private String id0000;

    /**
     * 订单ID
     */
    private String xtddh0;

    /**
     * 订单类型:1-支付,2-退款
     */
    private Integer ddlx00;

    /**
     * 商户订单号
     */
    private String fwddh0;

    /**
     * 应用ID
     */
    private String fwqdid;

    /**
     * 通知地址
     */
    private String ybtzdz;

    /**
     * 通知响应结果
     */
    private String tzyxjg;

    /**
     * 通知次数
     */
    private Integer tzcs00;

    /**
     * 最大通知次数, 默认6次
     */
    private Integer zdtzcs;

    /**
     * 通知状态,1-通知中,2-通知成功,3-通知失败
     */
    private Integer tzzt00;

    /**
     * 最后一次通知时间
     */
    private Date zhtzsj;

    /**
     * 创建时间
     */
    private Date cjsj00;

}
