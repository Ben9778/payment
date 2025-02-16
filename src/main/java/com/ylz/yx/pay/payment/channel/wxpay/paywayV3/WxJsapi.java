package com.ylz.yx.pay.payment.channel.wxpay.paywayV3;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.wxpay.WxpayPaymentService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayV3Util;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxJsapiOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxJsapiOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 微信 jsapi支付
 */
@Service("wxpayPaymentByJsapiV3Service") //Service Name需保持全局唯一性
public class WxJsapi extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        // 使用的是V2接口的预先校验
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        wxPayService.getConfig().setTradeType(WxPayConstants.TradeType.JSAPI);

        // 构造请求数据
        JSONObject reqJSON = buildV3OrderRequest(payZfdd00, mchAppConfigContext);

        String reqUrl = WxpayV3Util.NORMALMCH_URL_MAP.get(WxPayConstants.TradeType.JSAPI);  // 请求地址
        JSONObject payer = new JSONObject();
        payer.put("openid", bizRQ.getOpenid());
        reqJSON.put("payer", payer);

        // wxPayConfig 添加子商户参数
        if (StringUtils.isNotBlank(reqJSON.getString("sub_mchid")) && StringUtils.isNotBlank(reqJSON.getString("sub_appid"))) {
            reqUrl = WxpayV3Util.ISV_URL_MAP.get(WxPayConstants.TradeType.JSAPI);
            wxPayService.getConfig().setSubMchId(reqJSON.getString("sub_mchid"));
            wxPayService.getConfig().setSubAppId(reqJSON.getString("sub_appid"));
            reqJSON.put("payer", WxpayV3Util.processIsvPayer(reqJSON.getString("sub_appid"), bizRQ.getOpenid()));
        }

        // 构造函数响应数据
        WxJsapiOrderRS res = ApiResBuilder.buildSuccess(WxJsapiOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        try {
            JSONObject resJSON = WxpayV3Util.unifiedOrderV3(reqUrl, reqJSON, wxPayService);

            res.setPayInfo(resJSON.toJSONString());

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            //明确失败
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
