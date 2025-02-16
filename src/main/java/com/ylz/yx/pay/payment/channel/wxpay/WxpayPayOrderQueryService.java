package com.ylz.yx.pay.payment.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.channel.IPayOrderQueryService;
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

/*
* 微信查单接口
*/
@Service
public class WxpayPayOrderQueryService implements IPayOrderQueryService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public ChannelRetMsg query(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) {

        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayOrderQueryRequest req = new WxPayOrderQueryRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payZfdd00.getXtddh0());

                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayOrderQueryResult result = wxPayService.queryOrder(req);

                if("SUCCESS".equals(result.getTradeState())){ //支付成功
                    return ChannelRetMsg.confirmSuccess(result.getTransactionId());
                }else if("USERPAYING".equals(result.getTradeState())){ //支付中，等待用户输入密码
                    return ChannelRetMsg.waiting(); //支付中
                }else if("CLOSED".equals(result.getTradeState())
                        || "REVOKED".equals(result.getTradeState())
                        || "PAYERROR".equals(result.getTradeState())){  //CLOSED—已关闭， REVOKED—已撤销(刷卡支付), PAYERROR--支付失败(其他原因，如银行返回失败)
                    return ChannelRetMsg.confirmFail(); //支付失败
                }else{
                    return ChannelRetMsg.unknown();
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3

                String reqUrl;
                String query;

                WxpayMchParams mchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
                if (StringUtils.isNotBlank(mchParams.getSubMchId())) {
                    reqUrl = String.format("/v3/pay/partner/transactions/out-trade-no/%s", payZfdd00.getXtddh0());
                    query = String.format("?sp_mchid=%s&sub_mchid=%s", wxServiceWrapper.getWxPayService().getConfig().getMchId(), mchParams.getSubMchId());
                } else {
                    reqUrl = String.format("/v3/pay/transactions/out-trade-no/%s", payZfdd00.getXtddh0());
                    query = String.format("?mchid=%s", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                }

                JSONObject resultJSON = WxpayV3Util.queryOrderV3(reqUrl + query, wxServiceWrapper.getWxPayService());

                String channelState = resultJSON.getString("trade_state");
                if ("SUCCESS".equals(channelState)) {
                    return ChannelRetMsg.confirmSuccess(resultJSON.getString("transaction_id"));
                }else if("USERPAYING".equals(channelState)){ //支付中，等待用户输入密码
                    return ChannelRetMsg.waiting(); //支付中
                }else if("CLOSED".equals(channelState)
                        || "REVOKED".equals(channelState)
                        || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销(刷卡支付), PAYERROR--支付失败(其他原因，如银行返回失败)
                    return ChannelRetMsg.confirmFail(); //支付失败
                }else{
                    return ChannelRetMsg.unknown();
                }

            }else {
                return ChannelRetMsg.unknown();
            }

        } catch (WxPayException e) {
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

}
