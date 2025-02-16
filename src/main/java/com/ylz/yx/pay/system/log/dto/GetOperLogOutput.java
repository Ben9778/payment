package com.ylz.yx.pay.system.log.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GetOperLogOutput {
    /** 日志主键 */
    private String id0000;

    /** 操作模块 */
    private String czmk00;

    /** 业务类型（0=其它,1=新增,2=修改,3=删除） */
    private Integer ywlx00;

    /** 请求方法 */
    private String qqff00;

    /** 请求方式 */
    private String qqfs00;

    /** 操作人员 */
    private String ygbh00;

    /** 操作人员 */
    private String zwxm00;

    /** 操作人员 */
    private String jsmc00;

    /** 请求url */
    private String qqurl0;

    /** 请求参数 */
    private String qqcs00;

    /** 返回参数 */
    private String fhcs00;

    /** 操作状态（0正常 1异常） */
    private Integer czzt00;

    /** 错误信息 */
    private String cwxx00;

    /** 操作时间 */
    private String czsj00;
}
