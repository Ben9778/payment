package com.ylz.yx.pay.payment.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.ResponseException;
import com.ylz.yx.pay.core.model.params.alipay.AlipayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractChannelNoticeService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/*
* 支付宝 回调接口实现类
*/
@Service
public class AlipayChannelNoticeService extends AbstractChannelNoticeService {

    private static final Logger log = new Logger(AlipayChannelNoticeService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("out_trade_no");
            return MutablePair.of(payOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }



    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {

            //配置参数获取
            String alipaySignType, alipayPublicKey = null;

            // 获取支付参数
            AlipayMchParams alipayParams = (AlipayMchParams)configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());

            alipaySignType = alipayParams.getSignType();
            alipayPublicKey = alipayParams.getAlipayPublicKey();

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;

            boolean verifyResult = AlipaySignature.rsaCheckV1(jsonParams.toJavaObject(Map.class), alipayPublicKey, alipayParams.getCharset(), alipaySignType);

            //验签失败
            if(!verifyResult){
                throw ResponseException.buildText("ERROR");
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("SUCCESS");

            ChannelRetMsg result = new ChannelRetMsg();
            result.setChannelOrderId(jsonParams.getString("trade_no")); //渠道订单号
            result.setChannelUserId(jsonParams.getString("buyer_id")); //支付用户ID
            result.setResponseEntity(okResponse); //响应数据

            result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

            if("TRADE_SUCCESS".equals(jsonParams.getString("trade_status"))){
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

            }else if("TRADE_CLOSED".equals(jsonParams.getString("trade_status"))){
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);

            }

            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
