package com.ylz.yx.pay.system.dict.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class XtZd0000 implements Serializable {
    private static final long serialVersionUID = -5843321382500939080L;
    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 字典名称
     */
    private String zdmc00;

    /**
     * 字典key
     */
    private String key000;

    /**
     * 字典value
     */
    private String value0;

    /**
     * 序号（排序）
     */
    private Integer xh0000;

    /**
     * 父级字典ID
     */
    private String fjid00;

    /**
     * 备注
     */
    private String bz0000;

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

}