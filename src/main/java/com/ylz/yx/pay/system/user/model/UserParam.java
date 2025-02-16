package com.ylz.yx.pay.system.user.model;

import lombok.Data;

@Data
public class UserParam {

    /**
     * 员工编号
     */
    private String ygbh00;

    /**
     * 中文姓名
     */
    private String zwxm00;

    /**
     * 胸卡号
     */
    private String xkh000;

    /**
     * 用户口令
     */
    private String yhkl00;

    /**
     * '0'启用,'1'禁用
     */
    private String czbz00;

    /**
     * 角色ID
     */
    private String jsid00;

    /**
     * 创建人ID
     */
    private String cjr000;
}
