package com.ylz.yx.pay.payment.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.channel.IChannelUserService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * 微信支付 获取微信openID实现类
 */
@Service
public class WxpayChannelUserService implements IChannelUserService {

    private static final Logger log = new Logger(WxpayChannelUserService.class.getName());

    @Autowired
    private ConfigContextQueryService configContextQueryService;

    /**
     * 默认官方跳转地址
     **/
    private static final String DEFAULT_OAUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public String buildUserRedirectUrl(String callbackUrlEncode, MchAppConfigContext mchAppConfigContext) {

        String appId = null;
        String oauth2Url = "";
        //获取商户配置信息
        WxpayMchParams normalMchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), CS.IF_CODE.WXPAY);
        if (normalMchParams == null) {
            throw new BizException("商户微信支付接口没有配置！");
        }
        if (StringUtils.isNotBlank(normalMchParams.getSubMchAppId())) {
            appId = normalMchParams.getSubMchAppId();
        } else {
            appId = normalMchParams.getAppId();
        }

        oauth2Url = normalMchParams.getOauth2Url();

        if (StringUtils.isBlank(oauth2Url)) {
            oauth2Url = DEFAULT_OAUTH_URL;
        }
        String wxUserRedirectUrl = String.format(oauth2Url + "?appid=%s&scope=snsapi_base&state=&redirect_uri=%s&response_type=code#wechat_redirect", appId, callbackUrlEncode);
        log.info("wxUserRedirectUrl=" + wxUserRedirectUrl);
        return wxUserRedirectUrl;
    }

    @Override
    public String getChannelUserId(JSONObject reqParams, MchAppConfigContext mchAppConfigContext) {
        String code = reqParams.getString("code");
        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
            return wxServiceWrapper.getWxMpService().getOAuth2Service().getAccessToken(code).getOpenId();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

}
