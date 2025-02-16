package com.ylz.yx.pay.system.dict.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdDictParam {

    /**
     * 主键ID0000
     */
    @NotNull
    private String id0000;

    /**
     * 字典名称
     */
    @NotNull
    private String zdmc00;

    /**
     * 字典key
     */
    @NotNull
    private String key000;

    /**
     * 字典value
     */
    private String value0;

    /**
     * 父级字典ID
     */
    private String fjid00;

    /**
     * 是否启用
     */
    private String sfqy00;
}
