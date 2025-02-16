package com.ylz.yx.pay.payment.controller.qr;

import com.alipay.api.AlipayApiException;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.channel.IChannelUserService;
import com.ylz.yx.pay.payment.controller.payorder.AbstractPayOrderController;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliJsapiOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.WxJsapiOrderRQ;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.PayMchNotifyService;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import com.ylz.yx.pay.utils.PayKit;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/*
* 聚合码支付二维码收银台controller
*/
@RestController
@RequestMapping("/api/cashier")
public class QrCashierController extends AbstractPayOrderController {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private JdbcGateway jdbcGateway;
    @Resource private ApplicationProperty applicationProperty;
    @Autowired private PayMchNotifyService payMchNotifyService;

    /**
     * 返回 oauth2【获取uerId跳转地址】
     * **/
    @PostMapping("/redirectUrl")
    public Object redirectUrl(){

        //查询订单
        PayZfdd00 payZfdd00 = getPayOrder();

        //回调地址
        String redirectUrlEncode = applicationProperty.genOauth2RedirectUrlEncode(payZfdd00.getXtddh0());

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payZfdd00.getFwqdid());

        //获取接口并返回数据
        IChannelUserService channelUserService = getServiceByWayCode(getWayCode(), "ChannelUserService", IChannelUserService.class);
        return channelUserService.buildUserRedirectUrl(redirectUrlEncode, mchAppConfigContext);

    }

    /**
     * 获取userId
     * **/
    @PostMapping("/channelUserId")
    public Object channelUserId() throws Exception {

        //查询订单
        PayZfdd00 payZfdd00 = getPayOrder();

        String wayCode = getWayCode();

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payZfdd00.getFwqdid());
        IChannelUserService channelUserService = getServiceByWayCode(wayCode, "ChannelUserService", IChannelUserService.class);
        return channelUserService.getChannelUserId(getReqParamJSON(), mchAppConfigContext);

    }


    /**
     * 获取订单支付信息
     * **/
    @PostMapping("/payOrderInfo")
    public Object payOrderInfo() throws Exception {

        //查询订单
        PayZfdd00 payZfdd00 = getPayOrder();

        /*PayZfdd00 resOrder = new PayZfdd00();
        resOrder.setXtddh0(payZfdd00.getXtddh0());
        resOrder.setFwddh0(payZfdd00.getFwddh0());
        resOrder.setZfje00(payZfdd00.getZfje00());*/


       /* PayOrder resOrder = new PayOrder();
        resOrder.setPayOrderId(payOrder.getPayOrderId());
        resOrder.setMchOrderNo(payOrder.getMchOrderNo());
        resOrder.setMchName(payOrder.getMchName());
        resOrder.setAmount(payOrder.getAmount());
        resOrder.setReturnUrl(payMchNotifyService.createReturnUrl(payOrder, configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId()).getMchApp().getAppSecret()));
*/
        Map<String, Object> map = new HashMap<>();
        map.put("payOrderId", payZfdd00.getXtddh0());
        map.put("mchOrderNo", payZfdd00.getFwddh0());
        map.put("mchName", applicationProperty.getMchName());
        map.put("amount", payZfdd00.getZfje00());
        //map.put("returnUrl", payMchNotifyService.createReturnUrl(payZfdd00, configContextQueryService.queryMchApp(payZfdd00.getFwqdid()).getQdmy00()));

        return map;
    }


    /** 调起下单接口, 返回支付数据包  **/
    @PostMapping("/pay")
    public Object pay() throws Exception {

        //查询订单
        PayZfdd00 payZfdd00 = getPayOrder();

        String wayCode = getWayCode();

        ApiRes apiRes = null;

        if(wayCode.equals(CS.PAY_WAY_CODE.ALI_JSAPI)){
            apiRes = packageAlipayPayPackage(payZfdd00);
        }else if(wayCode.equals(CS.PAY_WAY_CODE.WX_JSAPI)){
            apiRes = packageWxpayPayPackage(payZfdd00);
        }

        return apiRes;
    }


    /** 获取支付宝的 支付参数 **/
    private ApiRes packageAlipayPayPackage(PayZfdd00 payZfdd00) throws AlipayApiException {
        String channelUserId = getValStringRequired("channelUserId");
        AliJsapiOrderRQ rq = new AliJsapiOrderRQ();
        rq.setBuyerUserId(channelUserId);
        return this.unifiedOrder(getWayCode(), rq, payZfdd00);
    }


    /** 获取微信的 支付参数 **/
    private ApiRes packageWxpayPayPackage(PayZfdd00 payZfdd00) throws AlipayApiException {
        String openId = getValStringRequired("channelUserId");
        WxJsapiOrderRQ rq = new WxJsapiOrderRQ();
        rq.setOpenid(openId);
        return this.unifiedOrder(getWayCode(), rq, payZfdd00);
    }


    private String getToken(){
        return getValStringRequired("token");
    }

    private String getWayCode(){
        return getValStringRequired("wayCode");
    }

    private PayZfdd00 getPayOrder(){

        String payOrderId = PayKit.aesDecode(getToken()); //解析token

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payOrderId);
        if(payZfdd00 == null || !payZfdd00.getDdzt00().equals(PayZfdd00.STATE_INIT)){
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在或状态不正确");
        }

        return payZfdd00;
    }


    private <T> T getServiceByWayCode(String wayCode, String serviceSuffix, Class<T> cls){

        if(CS.PAY_WAY_CODE.ALI_JSAPI.equals(wayCode)){
            return SpringBeansUtil.getBean(CS.IF_CODE.ALIPAY + serviceSuffix, cls);
        }else if(CS.PAY_WAY_CODE.WX_JSAPI.equals(wayCode)){
            return SpringBeansUtil.getBean(CS.IF_CODE.WXPAY + serviceSuffix, cls);
        }

        return null;
    }






}
