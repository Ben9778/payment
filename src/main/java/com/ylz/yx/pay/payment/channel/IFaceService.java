package com.ylz.yx.pay.payment.channel;

import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;

/*
* 刷脸设备初始化
*/
public interface IFaceService {

    /** 获取到接口code **/
    String getIfCode();

    /** 初始化 **/
    AbstractRS init(JSONObject reqParams, MchAppConfigContext mchAppConfigContext);

}
