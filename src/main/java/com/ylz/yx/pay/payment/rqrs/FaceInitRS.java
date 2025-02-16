package com.ylz.yx.pay.payment.rqrs;

import lombok.Data;

/*
* 刷脸设备初始化 响应参数
*/
@Data
public class FaceInitRS extends AbstractRS {

    /** 业务响应码 **/
    private Integer code;

    /** 业务响应信息 **/
    private String msg;

    /** SDK调用凭证 **/
    private String authinfo;

    /** ZIM上下文ID **/
    private String zimId;

    /** 客户端协议 **/
    private String zimInitClientData;

}
