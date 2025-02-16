package com.ylz.yx.pay.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.response.AlipayTradePayResponse;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.payment.channel.alipay.AlipayKit;
import com.ylz.yx.pay.payment.channel.alipay.AlipayPaymentService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliFaceOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliFaceOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import com.ylz.yx.pay.utils.AmountUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 支付宝 刷脸支付
 */
@Service("alipayPaymentByAliFaceService") //Service Name需保持全局唯一性
public class AliFace extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {

        AliFaceOrderRQ bizRQ = (AliFaceOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            return "用户支付条码[authCode]不可为空";
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext){

        AliFaceOrderRQ bizRQ = (AliFaceOrderRQ) rq;

        AlipayTradePayRequest req = new AlipayTradePayRequest();
        AlipayTradePayModel model = new AlipayTradePayModel();
        model.setOutTradeNo(payZfdd00.getXtddh0());
        model.setScene("security_code"); //刷脸支付 security_code
        model.setAuthCode(bizRQ.getAuthCode().trim()); //支付授权码
        model.setSubject(payZfdd00.getDdmc00()); //订单标题
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payZfdd00.getZfje00().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payZfdd00.getDdsxsj(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        //调起支付宝 （如果异常， 将直接跑出   ChannelException ）
        AlipayTradePayResponse alipayResp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);

        // 构造函数响应数据
        AliFaceOrderRS res = ApiResBuilder.buildSuccess(AliFaceOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelAttach(alipayResp.getBody());
        channelRetMsg.setChannelOrderId(alipayResp.getTradeNo());
        channelRetMsg.setChannelUserId(alipayResp.getBuyerUserId()); //渠道用户标识

        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓

        //当条码重复发起时，支付宝返回的code = 10003, subCode = null [等待用户支付], 此时需要特殊判断 = = 。
        if("10000".equals(alipayResp.getCode()) && alipayResp.isSuccess()){ //支付成功, 更新订单成功 || 等待支付宝的异步回调接口

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);


        }else if("10003".equals(alipayResp.getCode())){ //10003 表示为 处理中, 例如等待用户输入密码

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        }else{  //其他状态, 表示下单失败

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(AlipayKit.appendErrCode(alipayResp.getCode(), alipayResp.getSubCode()));
            channelRetMsg.setChannelErrMsg(AlipayKit.appendErrMsg(alipayResp.getMsg(), alipayResp.getSubMsg()));
        }

        return res;

    }
}
