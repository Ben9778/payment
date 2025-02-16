package com.ylz.yx.pay.payment.channel;


import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.util.ChannelCertConfigKitBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/*
* 退款接口抽象类
*/
public abstract class AbstractRefundService implements IRefundService{

    @Autowired protected ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired protected ConfigContextQueryService configContextQueryService;
    @Resource
    protected ApplicationProperty applicationProperty;

    protected String getNotifyUrl(){
        return applicationProperty.getPaySiteBackUrl() + "/api/refund/notify/" + getIfCode();
    }

    protected String getNotifyUrl(String refundOrderId){
        return applicationProperty.getPaySiteBackUrl() + "/api/refund/notify/" + getIfCode() + "/" + refundOrderId;
    }

}
