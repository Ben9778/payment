package com.ylz.yx.pay.payment.rqrs.payorder;

import com.alibaba.fastjson.annotation.JSONField;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import lombok.Data;

/*
* 创建订单(统一订单) 响应参数
*/
@Data
public class UnifiedOrderRS extends AbstractRS {

    /** 支付订单号 **/
    private String payOrderId;

    /** 商户订单号 **/
    private String mchOrderNo;

    /** 订单状态 **/
    private String orderState;

    /** 支付接口代码 **/
    private String ifCode;

    /** 支付参数类型  ( 无参数，  调起支付插件参数， 重定向到指定地址，  用户扫码   )   **/
    private String payDataType;

    /** 支付参数 **/
    private String payData;

    /** 渠道返回错误代码 **/
    private String errCode;

    /** 渠道返回错误信息 **/
    private String errMsg;

    /** 上游渠道返回数据包 (无需JSON序列化) **/
    @JSONField(serialize = false)
    private ChannelRetMsg channelRetMsg;

    /** 生成聚合支付参数 (仅统一下单接口使用) **/
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.NONE;
    }

    /** 生成支付参数 **/
    public String buildPayData(){
        return "";
    }


}
