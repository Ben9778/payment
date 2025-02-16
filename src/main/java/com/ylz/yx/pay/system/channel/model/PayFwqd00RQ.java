package com.ylz.yx.pay.system.channel.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务渠道表 请求参数
 * </p>
 */
@Data
public class PayFwqd00RQ implements Serializable {

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
     * 渠道密钥
     */
    private String qdmy00;

    /**
     * 渠道有效期
     */
    private String qdyxq0;

    /**
     * 创建时间
     */
    private Date cjsj00;

    /**
     * 是否启用（0：否；1：是）
     */
    private String sfqy00;

    /**
     * 是否删除（0：否；1：是）
     */
    private String sfsc00;

    /**
     * 是否自动充值（0：否；1：是）
     */
    private String sfzdcz;

    /**
     * 应用类型（0：医疗服务收款；1：非医疗服务收款）
     */
    private String yylx00;

    /**
     * 支付渠道集合
     **/
    private List<PayFwzfgxRQ> zfqds0;

}
