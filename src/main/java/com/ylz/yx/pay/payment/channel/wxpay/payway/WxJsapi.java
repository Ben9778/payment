package com.ylz.yx.pay.payment.channel.wxpay.payway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.payment.channel.wxpay.WxpayPaymentService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxJsapiOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxJsapiOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 微信 jsapi支付
 */
@Service("wxpayPaymentByJsapiService") //Service Name需保持全局唯一性
public class WxJsapi extends WxpayPaymentService {

    private static final Logger log = new Logger(WxJsapi.class.getName());

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {

        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getOpenid())){
            return "[openid]不可为空";
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;

        WxPayUnifiedOrderRequest req = buildUnifiedOrderRequest(payZfdd00, mchAppConfigContext);
        req.setTradeType(WxPayConstants.TradeType.JSAPI);
        if(StringUtils.isNotBlank(req.getSubAppId())){ // 特约商户 && 传了子商户appId
            req.setSubOpenid(bizRQ.getOpenid()); // 用户在子商户appid下的唯一标识
        }else {
            req.setOpenid(bizRQ.getOpenid());
        }

        // 构造函数响应数据
        WxJsapiOrderRS res = ApiResBuilder.buildSuccess(WxJsapiOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        try {
            WxPayMpOrderResult payResult = wxPayService.createOrder(req);
            JSONObject resJSON = (JSONObject) JSON.toJSON(payResult);
            resJSON.put("package", payResult.getPackageValue());

            res.setPayInfo(resJSON.toJSONString());

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            log.error("WxPayException:", e);
            //明确失败
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;

    }

}
