package com.ylz.yx.pay.payment.channel.ysfpay;

import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.ResponseException;
import com.ylz.yx.pay.core.model.params.ysf.YsfpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractChannelNoticeService;
import com.ylz.yx.pay.payment.channel.ysfpay.utils.YsfSignUtils;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 云闪付回调
 */
@Service
public class YsfpayChannelNoticeService extends AbstractChannelNoticeService {

    private static final Logger log = new Logger(YsfpayChannelNoticeService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("orderNo");
            return MutablePair.of(payOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {

            ChannelRetMsg result = ChannelRetMsg.confirmSuccess(null);

            String logPrefix = "【处理云闪付支付回调】";

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            log.info(logPrefix + " 回调参数, jsonParams：" + jsonParams);

            // 校验支付回调
            boolean verifyResult = verifyParams(jsonParams, payZfdd00, mchAppConfigContext);
            // 验证参数失败
            if (!verifyResult) {
                throw ResponseException.buildText("ERROR");
            }
            log.info(logPrefix + "验证支付通知数据及签名通过");

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("success");
            result.setResponseEntity(okResponse);
            result.setChannelOrderId(jsonParams.getString("transIndex"));
            return result;

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * 验证云闪付支付通知参数
     *
     * @return
     */
    public boolean verifyParams(JSONObject jsonParams, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) {

        String orderNo = jsonParams.getString("orderNo");        // 商户订单号
        String txnAmt = jsonParams.getString("txnAmt");        // 支付金额
        if (StringUtils.isEmpty(orderNo)) {
            log.info("订单ID为空 [orderNo]=" + orderNo);
            return false;
        }
        if (StringUtils.isEmpty(txnAmt)) {
            log.info("金额参数为空 [txnAmt] :" + txnAmt);
            return false;
        }

        YsfpayMchParams mchParams = (YsfpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());

        //验签
        String ysfpayPublicKey = mchParams.getYsfpayPublicKey();

        //验签失败
        if (!YsfSignUtils.validate((JSONObject) JSONObject.toJSON(jsonParams), ysfpayPublicKey)) {
            log.info("【云闪付回调】 验签失败！ 回调参数：parameter = " + jsonParams + ", ysfpayPublicKey= " + ysfpayPublicKey);
            return false;
        }

        // 核对金额
        long dbPayAmt = payZfdd00.getZfje00().longValue();
        if (dbPayAmt != Long.parseLong(txnAmt)) {
            log.info("订单金额与参数金额不符。 dbPayAmt=" + dbPayAmt + ", txnAmt=" + txnAmt + ", payOrderId=" + orderNo);
            return false;
        }
        return true;
    }

}
