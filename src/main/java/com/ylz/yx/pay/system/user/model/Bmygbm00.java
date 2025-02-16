package com.ylz.yx.pay.system.user.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Bmygbm00 implements Serializable {
    private static final long serialVersionUID = -5377141194332629802L;

    /**
     * 员工编号，内部号 SQ_BM_YGBM00_YGBH00
     */
    private Integer ygbh00;

    /**
     * 中文姓名
     */
    private String zwxm00;

    /**
     * 性别
     */
    private String xb0000;

    /**
     * 胸卡号
     */
    private String xkh000;

    /**
     * 所属部门编号
     */
    private Integer bmbh00;

    /**
     * '0'新增员工,可删除,'1'现有员工,不可删除,'2'该员工现不用,但不可删除
     */
    private String czbz00;

    /**
     * 医生职称等级
     */
    private String yslb00;

    /**
     * 用户口令
     */
    private String yhkl00;

    /**
     * 从 医生证件号码 扩展为全院职工的 执业证书编号
     */
    private String yszjhm;

    /**
     * 身份证编号
     */
    private String sfzbh0;

    /**
     * 出生日期
     */
    private String ygcsrq;

}