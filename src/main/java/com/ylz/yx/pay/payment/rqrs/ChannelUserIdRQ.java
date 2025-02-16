package com.ylz.yx.pay.payment.rqrs;

import lombok.Data;

import org.hibernate.validator.constraints.NotBlank;

/*
* 商户获取渠道用户ID 请求参数对象
*/
@Data
public class ChannelUserIdRQ extends AbstractMchAppRQ{

    /** 接口代码,  AUTO表示：自动获取 **/
    @NotBlank(message="接口代码不能为空")
    private String ifCode;

    /** 商户扩展参数，将原样返回 **/
    private String extParam;

    /** 回调地址 **/
    @NotBlank(message="回调地址不能为空")
    private String redirectUrl;

}
