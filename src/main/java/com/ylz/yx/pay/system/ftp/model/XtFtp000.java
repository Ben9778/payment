package com.ylz.yx.pay.system.ftp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class XtFtp000 implements Serializable {
    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 服务渠道ID
     */
    private String fwqdid;

    /**
     * 服务渠道ID
     */
    private String fwqdmc;

    /**
     * FTP地址
     */
    private String ftpdz0;

    /**
     * FTP账号
     */
    private String ftpzh0;

    /**
     * FTP密码
     */
    private String ftpmm0;

    /**
     * 文件名称
     */
    private String wjmc00;

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