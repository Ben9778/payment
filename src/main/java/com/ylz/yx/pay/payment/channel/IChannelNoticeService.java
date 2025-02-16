package com.ylz.yx.pay.payment.channel;

import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/*
* 渠道侧的支付订单通知解析实现 【分为同步跳转（doReturn）和异步回调(doNotify) 】
*/
public interface IChannelNoticeService {

    /** 通知类型 **/
    enum NoticeTypeEnum {
        DO_RETURN, //同步跳转
        DO_NOTIFY //异步回调
    }

    /** 获取到接口code **/
    String getIfCode();

    /** 解析参数： 订单号 和 请求参数
     *  异常需要自行捕捉，并返回null , 表示已响应数据。
     * **/
    MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum);

    /** 返回需要更新的订单状态 和响应数据 **/
    ChannelRetMsg doNotice(HttpServletRequest request,
                           Object params, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum);

    /** 数据库订单 状态更新异常 (仅异步通知使用) **/
    ResponseEntity doNotifyOrderStateUpdateFail(HttpServletRequest request);

    /** 数据库订单数据不存在  (仅异步通知使用) **/
    ResponseEntity doNotifyOrderNotExists(HttpServletRequest request);

}
