package com.ylz.yx.pay.payment.rqrs;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/*
* 刷脸设备初始化 请求参数对象
*/
@Data
public class FaceInitRQ extends AbstractMchAppRQ{

    /** 接口代码,  AUTO表示：自动获取 **/
    @NotBlank(message="接口代码不能为空")
    private String ifCode;

    /** 商户扩展参数，将原样返回 **/
    private String extParam;

}
