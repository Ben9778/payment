package com.ylz.yx.pay.payment.channel.wxpay.payway;


import com.github.binarywang.wxpay.bean.request.WxPayFacepayRequest;
import com.github.binarywang.wxpay.bean.result.WxPayFacepayResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.payment.channel.wxpay.WxpayPaymentService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxFaceOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxFaceOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 微信 刷脸支付
 */
@Service("wxpayPaymentByFaceService") //Service Name需保持全局唯一性
public class WxFace extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {

        WxFaceOrderRQ bizRQ = (WxFaceOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            return "用户支付条码[authCode]不可为空";
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxFaceOrderRQ bizRQ = (WxFaceOrderRQ) rq;

        // 微信统一下单请求对象
        WxPayFacepayRequest request = new WxPayFacepayRequest();
        request.setOutTradeNo(payZfdd00.getXtddh0());
        request.setBody(payZfdd00.getDdmc00());
        request.setFeeType("CNY");
        request.setTotalFee(payZfdd00.getZfje00().intValue());
        request.setSpbillCreateIp(StringUtils.defaultIfEmpty(bizRQ.getClientIp().trim(), "127.0.0.1"));
        request.setFaceCode(bizRQ.getAuthCode().trim());

        //放置isv信息
        WxpayKit.putApiIsvInfo(mchAppConfigContext, request);

        // 构造函数响应数据
        WxFaceOrderRS res = ApiResBuilder.buildSuccess(WxFaceOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        try {
            WxPayFacepayResult wxPayMicropayResult = wxPayService.facepay(request);

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
