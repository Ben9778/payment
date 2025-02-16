package com.ylz.yx.pay.payment.rqrs;

import lombok.Data;

import org.hibernate.validator.constraints.NotBlank;

/*
* 通用RQ, appId 必填项
*/
@Data
public class AbstractMchAppRQ extends AbstractRQ {

    /** 渠道ID **/
    @NotBlank(message="渠道ID不能为空")
    private String appId;


}
