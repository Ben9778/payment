package com.ylz.yx.pay.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.utils.AmountUtil;
import com.ylz.yx.pay.payment.channel.alipay.AlipayKit;
import com.ylz.yx.pay.payment.channel.alipay.AlipayPaymentService;
import com.ylz.yx.pay.payment.exception.ChannelException;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliPcOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliPcOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.springframework.stereotype.Service;

/*
* 支付宝 PC支付
*/
@Service("alipayPaymentByAliPcService") //Service Name需保持全局唯一性
public class AliPc extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext){

        AliPcOrderRQ bizRQ = (AliPcOrderRQ) rq;

        AlipayTradePagePayRequest req = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payZfdd00.getXtddh0());
        model.setSubject(payZfdd00.getDdmc00()); //订单标题
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payZfdd00.getZfje00().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payZfdd00.getDdsxsj(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setQrPayMode("2"); //订单码-跳转模式
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setReturnUrl(getReturnUrl()); // 同步跳转地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        // 构造函数响应数据
        AliPcOrderRS res = ApiResBuilder.buildSuccess(AliPcOrderRS.class);

        try {
            if(CS.PAY_DATA_TYPE.FORM.equals(bizRQ.getPayDataType())){
                res.setFormContent(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req).getBody());
            }else{
                res.setPayUrl(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req, "GET").getBody());
            }
        }catch (AlipayApiException e) {
            throw ChannelException.sysError(e.getMessage());
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        return res;
    }

}
