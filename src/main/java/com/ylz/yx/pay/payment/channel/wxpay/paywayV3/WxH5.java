package com.ylz.yx.pay.payment.channel.wxpay.paywayV3;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.wxpay.WxpayPaymentService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayV3Util;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxH5OrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxH5OrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 微信 H5支付
 */
@Service("wxpayPaymentByH5V3Service") //Service Name需保持全局唯一性
public class WxH5 extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) {

        WxH5OrderRQ bizRQ = (WxH5OrderRQ) rq;

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        wxPayService.getConfig().setTradeType(WxPayConstants.TradeType.MWEB);

        // 构造请求数据
        JSONObject reqJSON = buildV3OrderRequest(payZfdd00, mchAppConfigContext);

        JSONObject sceneInfo = reqJSON.getJSONObject("scene_info");

        JSONObject h5Info = new JSONObject();
        h5Info.put("type", "iOS, Android, Wap");
        sceneInfo.put("h5_info", h5Info);

        reqJSON.put("scene_info", sceneInfo);

        String reqUrl;  // 请求地址
        if (StringUtils.isNotBlank(reqJSON.getString("sub_mchid")) && StringUtils.isNotBlank(reqJSON.getString("sub_appid"))) {
            reqUrl = WxpayV3Util.ISV_URL_MAP.get(WxPayConstants.TradeType.MWEB);
        } else {
            reqUrl = WxpayV3Util.NORMALMCH_URL_MAP.get(WxPayConstants.TradeType.MWEB);
        }

        // 构造函数响应数据
        WxH5OrderRS res = ApiResBuilder.buildSuccess(WxH5OrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        try {
            JSONObject resJSON = WxpayV3Util.unifiedOrderV3(reqUrl, reqJSON, wxPayService);

            String payUrl = resJSON.getString("h5_url");
            payUrl = applicationProperty.getPaySiteBackUrl() + "/api/common/payUrl/" + Base64.encode(payUrl);
            if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())){ //二维码图片地址
                res.setCodeImgUrl(applicationProperty.genScanImgUrl(payUrl));
            }else{ // 默认都为 payUrl方式
                res.setPayUrl(payUrl);
            }

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
