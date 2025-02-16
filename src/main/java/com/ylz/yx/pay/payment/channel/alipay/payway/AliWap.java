package com.ylz.yx.pay.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
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
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliWapOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliWapOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.springframework.stereotype.Service;

/*
* 支付宝 wap支付
*/
@Service("alipayPaymentByAliWapService") //Service Name需保持全局唯一性
public class AliWap extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext){

        AliWapOrderRQ bizRQ = (AliWapOrderRQ)rq;

        AlipayTradeWapPayRequest req = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payZfdd00.getXtddh0());
        model.setSubject(payZfdd00.getDdmc00()); //订单标题
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payZfdd00.getZfje00().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payZfdd00.getDdsxsj(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        model.setProductCode("QUICK_WAP_PAY");
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setReturnUrl(getReturnUrl()); // 同步跳转地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        // 构造函数响应数据
        AliWapOrderRS res = ApiResBuilder.buildSuccess(AliWapOrderRS.class);

        try {
            if(CS.PAY_DATA_TYPE.FORM.equals(bizRQ.getPayDataType())){ //表单方式
                res.setFormContent(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req).getBody());
            }else if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())){ //二维码图片地址
                String payUrl = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req, "GET").getBody();
                res.setCodeImgUrl(applicationProperty.genScanImgUrl(payUrl));
            }else{ // 默认都为 payUrl方式
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
