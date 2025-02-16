package com.ylz.yx.pay.payment.channel.alipay;

import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.channel.AbstractRefundService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRQ;
import com.ylz.yx.pay.utils.AmountUtil;
import org.springframework.stereotype.Service;

/*
* 退款接口： 支付宝官方
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/17 9:38
*/
@Service
public class AlipayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(payTkdd00.getYxtddh());
        model.setTradeNo(payTkdd00.getYzfddh());
        model.setOutRequestNo(payTkdd00.getXtddh0());
        model.setRefundAmount(AmountUtil.convertCent2Dollar(payTkdd00.getTkje00().toString()));
        model.setRefundReason(payTkdd00.getTkyy00());
        request.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

        AlipayTradeRefundResponse response = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        channelRetMsg.setChannelAttach(response.getBody());

        // 调用成功
        if(response.isSuccess()){
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            channelRetMsg.setChannelOrderId(response.getTradeNo());
        }else{

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(response.getSubCode());
            channelRetMsg.setChannelErrMsg(response.getSubMsg());
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(PayTkdd00 payTkdd00, MchAppConfigContext mchAppConfigContext) throws Exception {

        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setTradeNo(payTkdd00.getYzfddh());
        model.setOutTradeNo(payTkdd00.getYxtddh());
        model.setOutRequestNo(payTkdd00.getXtddh0());
        request.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

        AlipayTradeFastpayRefundQueryResponse response = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        channelRetMsg.setChannelAttach(response.getBody());

        // 调用成功 & 金额相等  （传入不存在的outRequestNo支付宝仍然返回响应成功只是数据不存在， 调用isSuccess() 仍是成功, 此处需判断金额是否相等）
        Long channelRefundAmount = response.getRefundAmount() == null ? null : Long.parseLong(AmountUtil.convertDollar2Cent(response.getRefundAmount()));
        if(response.isSuccess() && payTkdd00.getTkje00().equals(channelRefundAmount)){
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            channelRetMsg.setChannelOrderId(response.getTradeNo());
        }else{

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING); //认为是处理中

        }

        return channelRetMsg;
    }


}
