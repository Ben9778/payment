package com.ylz.yx.pay.system.dict.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class GlobalDict implements Serializable {
    private static final long serialVersionUID = -6159331443989795484L;
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
     * 创建时间
     */
    private Date cjsj00;

    /**
     * 序号（排序）
     */
    private Integer xh0000;

    /**
     * 父级字典ID
     */
    private String fjid00;

    private Map<String,GlobalDict> children = new HashMap<String,GlobalDict>();

}
