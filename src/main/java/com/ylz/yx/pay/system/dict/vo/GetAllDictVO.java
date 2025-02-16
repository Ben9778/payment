package com.ylz.yx.pay.system.dict.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class GetAllDictVO implements Serializable {
    private static final long serialVersionUID = -8833203996813909240L;

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

    private List<GetAllDictVO> children = new ArrayList<GetAllDictVO>();

    public void addChild(GetAllDictVO child){
        this.children.add(child);
    }

}
