package com.ylz.yx.pay.payment.channel.ysfpay;

import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.channel.AbstractRefundService;
import com.ylz.yx.pay.payment.channel.ysfpay.utils.YsfHttpUtil;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * 退款接口： 云闪付官方
 */
@Service
public class YsfpayRefundService extends AbstractRefundService {

    private static final Logger log = new Logger(YsfpayRefundService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Autowired
    private YsfpayPaymentService ysfpayPaymentService;

    @Override
    public String preCheck(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payZfdd00.getZffs00());
        String logPrefix = "【云闪付(" + orderType + ")退款】";
        try {
            reqParams.put("origOrderNo", payZfdd00.getXtddh0()); // 原交易订单号
            reqParams.put("origTxnAmt", payZfdd00.getZfje00()); // 原交易金额
            reqParams.put("orderNo", payTkdd00.getXtddh0()); // 退款订单号
            reqParams.put("txnAmt ", payTkdd00.getTkje00()); // 退款金额
            reqParams.put("orderType ", orderType); // 订单类型

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/refund", reqParams, logPrefix, mchAppConfigContext);
            log.info("查询订单 payorderId:" + payZfdd00.getXtddh0() + ", 返回结果:" + resJSON);
            if (resJSON == null) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN); // 状态不明确
            }
            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            channelRetMsg.setChannelOrderId(payTkdd00.getXtddh0());
            if ("00".equals(respCode)) { // 交易成功
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                channelRetMsg.setChannelOrderId(resJSON.getString("queryId"));
                log.info(logPrefix + " >>> 退款成功");
            } else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
                log.info(logPrefix + " >>> 退款失败, " + respMsg);
            }
        } catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(PayTkdd00 payTkdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payTkdd00.getZffs00());
        String logPrefix = "【云闪付(" + orderType + ")退款查询】";
        try {
            reqParams.put("orderNo", payTkdd00.getXtddh0()); // 退款订单号
            reqParams.put("origOrderNo", payTkdd00.getYxtddh()); // 原交易订单号

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/refundQuery", reqParams, logPrefix, mchAppConfigContext);
            log.info("查询订单 refundOrderId:" + payTkdd00.getXtddh0() + ", 返回结果:" + resJSON);
            if (resJSON == null) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN); // 状态不明确
            }
            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            String origRespCode = resJSON.getString("origRespCode"); //原交易应答码
            String origRespMsg = resJSON.getString("origRespMsg"); //原交易应答信息
            channelRetMsg.setChannelOrderId(payTkdd00.getXtddh0());
            if ("00".equals(respCode)) { // 请求成功
                if ("00".equals(origRespCode)) { //明确退款成功

                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    log.info(logPrefix + " >>> 退款成功");

                } else if ("01".equals(origRespCode)) { //明确退款失败

                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(respCode);
                    channelRetMsg.setChannelErrMsg(respMsg);
                    log.info(logPrefix + " >>> 退款失败, " + respMsg);

                } else if ("02".equals(origRespCode)) { //退款中
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    log.info(logPrefix + "{} >>> 退款中");
                }
            } else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
                log.info(logPrefix + " >>> 退款失败, " + respMsg);
            }
        } catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

}
