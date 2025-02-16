package com.ylz.yx.pay.payment.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayRefundQueryRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractRefundService;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayKit;
import com.ylz.yx.pay.payment.channel.wxpay.kits.WxpayV3Util;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 退款接口： 微信官方
 */
@Service
public class WxpayRefundService extends AbstractRefundService {

    private static final Logger log = new Logger(WxpayRefundService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00) {
        return null;
    }

    /** 微信退款接口 **/
    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        try {

            ChannelRetMsg channelRetMsg = new ChannelRetMsg();

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayRefundRequest req = new WxPayRefundRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payZfdd00.getXtddh0());    // 商户订单号
                req.setOutRefundNo(payTkdd00.getXtddh0()); // 退款单号
                req.setTotalFee(payZfdd00.getZfje00().intValue());   // 订单总金额
                req.setRefundFee(payTkdd00.getTkje00().intValue()); // 退款金额
                req.setNotifyUrl(getNotifyUrl(payTkdd00.getXtddh0()));   // 回调url
                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayRefundResult result = wxPayService.refundV2(req);
                if("SUCCESS".equals(result.getResultCode())){ // 退款发起成功,结果主动查询
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelOrderId(result.getRefundId());
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(result.getErrCode());
                    channelRetMsg.setChannelErrMsg(WxpayKit.appendErrMsg(result.getReturnMsg(), result.getErrCodeDes()));
                }
            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3
                // 微信统一下单请求对象
                JSONObject reqJSON = new JSONObject();
                reqJSON.put("out_trade_no", payTkdd00.getYxtddh());   // 订单号
                reqJSON.put("out_refund_no", payTkdd00.getXtddh0()); // 退款订单号
                reqJSON.put("notify_url", getNotifyUrl(payTkdd00.getXtddh0())); // 回调地址

                JSONObject amountJson = new JSONObject();
                amountJson.put("refund", payTkdd00.getTkje00());// 退款金额
                amountJson.put("total", payZfdd00.getZfje00());// 订单总金额
                amountJson.put("currency", "CNY");// 币种
                reqJSON.put("amount", amountJson);

                WxpayMchParams mchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
                if (StringUtils.isNotBlank(mchParams.getSubMchId())) {
                    reqJSON.put("sub_mchid", mchParams.getSubMchId());
                }

                JSONObject resultJSON = WxpayV3Util.refundV3(reqJSON, wxServiceWrapper.getWxPayService());
                String status = resultJSON.getString("status");
                if("SUCCESS".equals(status)){ // 退款成功
                    String refundId = resultJSON.getString("refund_id");
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    channelRetMsg.setChannelOrderId(refundId);
                }else if ("PROCESSING".equals(status)){ // 退款处理中
                    String refundId = resultJSON.getString("refund_id");
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelOrderId(refundId);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrMsg(status);
                }

            }
            return channelRetMsg;
        } catch (WxPayException e) {
            log.error("微信退款WxPayException异常: ", e);
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
            return channelRetMsg;

        } catch (Exception e) {
            log.error("微信退款Exception异常: ", e);
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

    /** 微信退款查单接口 **/
    @Override
    public ChannelRetMsg query(PayTkdd00 payTkdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        try {
            ChannelRetMsg channelRetMsg = new ChannelRetMsg();

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);


            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayRefundQueryRequest req = new WxPayRefundQueryRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutRefundNo(payTkdd00.getXtddh0()); // 退款单号
                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayRefundQueryResult result = wxPayService.refundQueryV2(req);
                if("SUCCESS".equals(result.getResultCode())){ // 退款成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelErrMsg(result.getReturnMsg());
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3
                WxPayService wxPayService = wxServiceWrapper.getWxPayService();
                JSONObject resultJSON = null;

                WxpayMchParams mchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
                if (StringUtils.isNotBlank(mchParams.getSubMchId())) {
                    wxPayService.getConfig().setSubMchId(mchParams.getSubMchId());
                    resultJSON = WxpayV3Util.refundQueryV3Isv(payTkdd00.getXtddh0(), wxPayService);
                }else {
                    resultJSON = WxpayV3Util.refundQueryV3(payTkdd00.getXtddh0(), wxPayService);
                }
                String status = resultJSON.getString("status");
                if("SUCCESS".equals(status)){ // 退款成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelErrMsg(status);
                }
            }
            return channelRetMsg;
        } catch (WxPayException e) {
            log.error("微信退款查询WxPayException异常: ", e);
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            log.error("微信退款查询Exception异常: ", e);
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

}
