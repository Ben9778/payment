package com.ylz.yx.pay.payment.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayOrderCloseRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderCloseResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.channel.IPayOrderCloseService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayV3Util;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 微信关闭订单
 */
@Service
public class WxpayPayOrderCloseService implements IPayOrderCloseService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public ChannelRetMsg close(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) {

        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayOrderCloseRequest req = new WxPayOrderCloseRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payZfdd00.getXtddh0());

                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayOrderCloseResult result = wxPayService.closeOrder(req);

                if("SUCCESS".equals(result.getResultCode())){ //关闭订单成功
                    return ChannelRetMsg.confirmSuccess(null);
                }else if("FAIL".equals(result.getResultCode())){ //关闭订单失败
                    return ChannelRetMsg.confirmFail(); //关闭失败
                }else{
                    return ChannelRetMsg.waiting(); //关闭中
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3

                String reqUrl;
                JSONObject reqJson = new JSONObject();

                WxpayMchParams mchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
                if (StringUtils.isNotBlank(mchParams.getSubMchId())) {
                    reqUrl = String.format("/v3/pay/partner/transactions/out-trade-no/%s/close", payZfdd00.getXtddh0());
                    reqJson.put("sp_mchid", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                    reqJson.put("sub_mchid", mchParams.getSubMchId());
                } else {
                    reqUrl = String.format("/v3/pay/transactions/out-trade-no/%s/close", payZfdd00.getXtddh0());
                    reqJson.put("mchid", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                }

                WxpayV3Util.closeOrderV3(reqUrl, reqJson, wxServiceWrapper.getWxPayService());
                return ChannelRetMsg.confirmSuccess(null);
            }
            return ChannelRetMsg.confirmFail();
        } catch (WxPayException e) {
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

}
