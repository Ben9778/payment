package com.ylz.yx.pay.system.channel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务渠道表 返回响应参数
 * </p>
 */
@Data
public class PayFwqd00RS implements Serializable {

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
     * 支付渠道名称
     */
    private String zfqdmc;

    /**
     * 渠道密钥
     */
    private String qdmy00;

    /**
     * 渠道有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date qdyxq0;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    private List<PayFwzfgxRS> zfqds0;

}
