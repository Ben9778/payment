package com.ylz.yx.pay.payment.channel.wxpay.payway;


import com.github.binarywang.wxpay.bean.request.WxPayMicropayRequest;
import com.github.binarywang.wxpay.bean.result.WxPayMicropayResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.payment.channel.wxpay.WxpayPaymentService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxBarOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxBarOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 微信 bar
 */
@Service("wxpayPaymentByBarService") //Service Name需保持全局唯一性
public class WxBar extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {

        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            return "用户支付条码[authCode]不可为空";
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;

        // 微信统一下单请求对象
        WxPayMicropayRequest request = new WxPayMicropayRequest();
        request.setOutTradeNo(payZfdd00.getXtddh0());
        request.setBody(payZfdd00.getDdmc00());
        request.setFeeType("CNY");
        request.setTotalFee(payZfdd00.getZfje00().intValue());
        request.setSpbillCreateIp(StringUtils.defaultIfEmpty(bizRQ.getClientIp().trim(), "127.0.0.1"));
        request.setAuthCode(bizRQ.getAuthCode().trim());
        if(payZfdd00.getDdmc00().contains("住院")){
            request.setGoodsTag("HOSPITALIZATION");
        } else {
            request.setGoodsTag("MEDICINE_AND_TEST");
        }

        //放置isv信息
        WxpayKit.putApiIsvInfo(mchAppConfigContext, request);

        // 构造函数响应数据
        WxBarOrderRS res = ApiResBuilder.buildSuccess(WxBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        try {
            WxPayMicropayResult wxPayMicropayResult = wxPayService.micropay(request);

            channelRetMsg.setChannelOrderId(wxPayMicropayResult.getTransactionId());
            channelRetMsg.setChannelUserId(wxPayMicropayResult.getOpenid());
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

        } catch (WxPayException e) {
            //微信返回支付状态为【支付结果未知】, 需进行查单操作
            if("SYSTEMERROR".equals(e.getErrCode()) || "USERPAYING".equals(e.getErrCode()) ||  "BANKERROR".equals(e.getErrCode())){

                //轮询查询订单
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                channelRetMsg.setNeedQuery(true);
            }else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                WxpayKit.commonSetErrInfo(channelRetMsg, e);
            }
        }

        return res;
    }

}
