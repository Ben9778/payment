package com.ylz.yx.pay.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 支付渠道表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayZfqd00 implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 渠道编码
     */
    private String qdbm00;

    /**
     * 渠道名称
     */
    private String qdmc00;

    /**
     * 支付渠道名称
     */
    private String zfqdmc;

    /**
     * 渠道配置
     */
    private String qdpz00;

    /**
     * 支付方式["ali_bar", "ali_qr"]
     */
    private String zffs00;

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
     * 应用类型（0：医疗服务收款；1：非医疗服务收款）
     */
    private String yylx00;

}
