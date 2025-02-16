package com.ylz.yx.pay.payment.channel.wxpay.payway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.payment.channel.wxpay.WxpayPaymentService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxAppOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.springframework.stereotype.Service;

/*
* 微信 app支付
*/
@Service("wxpayPaymentByAppService") //Service Name需保持全局唯一性
public class WxApp extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxPayUnifiedOrderRequest req = buildUnifiedOrderRequest(payZfdd00, mchAppConfigContext);
        req.setTradeType(WxPayConstants.TradeType.APP);

        // 构造函数响应数据
        WxAppOrderRS res = ApiResBuilder.buildSuccess(WxAppOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        try {
            WxPayAppOrderResult payResult = wxPayService.createOrder(req);
            JSONObject resJSON = (JSONObject) JSON.toJSON(payResult);
            resJSON.put("package", payResult.getPackageValue());

            res.setPayInfo(resJSON.toJSONString());

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
