package com.ylz.yx.pay.payment.controller.qr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.payment.channel.IChannelUserService;
import com.ylz.yx.pay.payment.controller.payorder.AbstractPayOrderController;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.ChannelUserIdRQ;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.utils.PayKit;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 商户获取渠道用户ID接口
*/
@RestController
@RequestMapping("/api/channelUserId")
public class ChannelUserIdController extends AbstractPayOrderController {

    @Autowired private ConfigContextQueryService configContextQueryService;

    /**  重定向到微信地址  **/
    @RequestMapping("/jump")
    public void jump() throws Exception {

        //获取请求数据
        ChannelUserIdRQ rq = getRQByWithMchSign(ChannelUserIdRQ.class);

        String ifCode = "AUTO".equalsIgnoreCase(rq.getIfCode()) ? getIfCodeByUA() : rq.getIfCode();

        // 获取接口
        IChannelUserService channelUserService = SpringBeansUtil.getBean(ifCode + "ChannelUserService", IChannelUserService.class);

        if(channelUserService == null){
            throw new BizException("不支持的客户端");
        }

        if(!StringKit.isAvailableUrl(rq.getRedirectUrl())){
            throw new BizException("跳转地址有误！");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appId", rq.getAppId());
        jsonObject.put("extParam", rq.getExtParam());
        jsonObject.put("ifCode", ifCode);
        jsonObject.put("redirectUrl", rq.getRedirectUrl());

        //回调地址
        String callbackUrl = applicationProperty.genMchChannelUserIdApiOauth2RedirectUrlEncode(jsonObject);

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(rq.getAppId());
        String redirectUrl = channelUserService.buildUserRedirectUrl(callbackUrl, mchAppConfigContext);
        response.sendRedirect(redirectUrl);

    }


    /**  回调地址  **/
    @RequestMapping("/oauth2Callback/{aesData}")
    public void oauth2Callback(@PathVariable("aesData") String aesData) throws Exception {

        JSONObject callbackData = JSON.parseObject(PayKit.aesDecode(aesData));

        String appId = callbackData.getString("appId");
        String ifCode = callbackData.getString("ifCode");
        String extParam = callbackData.getString("extParam");
        String redirectUrl = callbackData.getString("redirectUrl");

        // 获取接口
        IChannelUserService channelUserService = SpringBeansUtil.getBean(ifCode + "ChannelUserService", IChannelUserService.class);

        if(channelUserService == null){
            throw new BizException("不支持的客户端");
        }

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(appId);

        //获取渠道用户ID
        String channelUserId = channelUserService.getChannelUserId(getReqParamJSON(), mchAppConfigContext);

        //同步跳转
        JSONObject appendParams = new JSONObject();
        appendParams.put("appId", appId);
        appendParams.put("channelUserId", channelUserId);
        appendParams.put("extParam", extParam);
        response.sendRedirect(StringKit.appendUrlQuery(redirectUrl, appendParams));
    }


    /** 根据UA获取支付接口 */
    private String getIfCodeByUA() {

        String ua = request.getHeader("User-Agent");

        // 无法识别扫码客户端
        if (StringUtils.isBlank(ua)) {
            return null;
        }

        if(ua.contains("Alipay")) {
            return CS.IF_CODE.ALIPAY;  //支付宝服务窗支付
        }else if(ua.contains("MicroMessenger")) {
            return CS.IF_CODE.WXPAY;
        }
        return null;
    }

}
