package com.ylz.yx.pay.system.role.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Ptjsb000 implements Serializable {
    private static final long serialVersionUID = 697120319983480397L;

    private Long jsid00;// 角色ID

    private String jsmc00;// 角色名称

    private String jsdesc;// 角色名称描述

    private Integer zzjgid;// 创建所属组织

    private Long cjr000;// 创建人

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cjrq00;// 创建日期

    private String jslx00;// 角色类型

    private String sfyx00;// 是否有效 0:无效，1：有效

    private String zbdm00;// 组别代码,F.K=XT_XTZB00.ZBDM00

    private String cjrxm0;// 创建人姓名

}
