package com.ylz.yx.pay.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.ylz.yx.pay.payment.channel.alipay.AlipayKit;
import com.ylz.yx.pay.payment.channel.alipay.AlipayPaymentService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.exception.ChannelException;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliAppOrderRS;
import com.ylz.yx.pay.utils.AmountUtil;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.springframework.stereotype.Service;

/*
* 支付宝 APP支付
*/
@Service("alipayPaymentByAliAppService") //Service Name需保持全局唯一性
public class AliApp extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext){

        AlipayTradeAppPayRequest req = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(payZfdd00.getXtddh0());//商户订单号
        model.setSubject(payZfdd00.getDdmc00()); //订单标题
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payZfdd00.getZfje00().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payZfdd00.getDdsxsj(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);


        String payData = null;

        // sdk方式需自行拦截接口异常信息
        try {
            payData = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().sdkExecute(req).getBody();
        } catch (AlipayApiException e) {
            throw ChannelException.sysError(e.getMessage());
        }

        // 构造函数响应数据
        AliAppOrderRS res = ApiResBuilder.buildSuccess(AliAppOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelAttach(payData);
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        res.setPayData(payData);
        return res;
    }

}
