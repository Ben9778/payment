package com.ylz.yx.pay.payment.channel;


import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.util.ChannelCertConfigKitBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/*
* 支付接口抽象类
*/
public abstract class AbstractPaymentService implements IPaymentService{

    @Autowired protected ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired protected ConfigContextQueryService configContextQueryService;
    @Resource
    protected ApplicationProperty applicationProperty;
    

    protected String getNotifyUrl(){
        return applicationProperty.getPaySiteBackUrl() + "/api/pay/notify/" + getIfCode();
    }

    protected String getNotifyUrl(String payOrderId){
        return applicationProperty.getPaySiteBackUrl() + "/api/pay/notify/" + getIfCode() + "/" + payOrderId;
    }

    protected String getReturnUrl(){
        return applicationProperty.getPaySiteBackUrl() + "/api/pay/return/" + getIfCode();
    }

    protected String getReturnUrl(String payOrderId){
        return applicationProperty.getPaySiteBackUrl() + "/api/pay/return/" + getIfCode() + "/" + payOrderId;
    }

}
