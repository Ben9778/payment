package com.ylz.yx.pay.system.user.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Personnel implements Serializable {
    private static final long serialVersionUID = -4846310352659499776L;

    private String xkh000;//胸卡号

    private String zwxm00;//中文姓名

    private Integer ygbh00;// 员工编号

    private String czbz00; // '0'启用,'1'禁用

    private String jsmc00; // 角色名称

    private Long jsid00;// 角色ID

    private String cjsj00; //创建时间

}
