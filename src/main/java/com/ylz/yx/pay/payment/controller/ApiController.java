package com.ylz.yx.pay.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.ctrl.AbstractCtrl;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractMchAppRQ;
import com.ylz.yx.pay.payment.rqrs.AbstractRQ;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.utils.PayKit;
import com.ylz.yx.pay.utils.ValidateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/*
* api 抽象接口， 公共函数
*/
public abstract class ApiController extends AbstractCtrl {

    @Autowired private ConfigContextQueryService configContextQueryService;


    /** 获取请求参数并转换为对象，通用验证  **/
    protected <T extends AbstractRQ> T getRQ(Class<T> cls){

        T bizRQ = getObject(cls);

        // [1]. 验证通用字段规则
        ValidateUtils.validate(bizRQ);

        return bizRQ;
    }


    /** 获取请求参数并转换为对象，商户通用验证  **/
    protected <T extends AbstractRQ> T getRQByWithMchSign(Class<T> cls){

        //获取请求RQ, and 通用验证
        T bizRQ = getRQ(cls);

        AbstractMchAppRQ abstractMchAppRQ = (AbstractMchAppRQ)bizRQ;

        //业务校验， 包括： 验签， 商户状态是否可用， 是否支持该支付方式下单等。
        String appId = abstractMchAppRQ.getAppId();
        String sign = bizRQ.getSign();

        if(StringUtils.isAnyBlank(appId, sign)){
            throw new BizException("参数有误！");
        }
        //从数据库查询服务渠道信息数据
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(appId);

        if(mchAppConfigContext == null){
            throw new BizException("服务渠道不存在");
        }

        if(mchAppConfigContext.getPayFwqd00() == null || mchAppConfigContext.getPayFwqd00().getSfqy00().equals(CS.NO)){
            throw new BizException("服务渠道状态不可用");
        }

        // 验签
        String appSecret = mchAppConfigContext.getPayFwqd00().getQdmy00();

        // 转换为 JSON
        JSONObject bizReqJSON = (JSONObject)JSONObject.toJSON(bizRQ);
        //把签名参数移除，其余参数进行验证
        bizReqJSON.remove("sign");
        if(!sign.equalsIgnoreCase(PayKit.getSign(bizReqJSON, appSecret))){
             throw new BizException("验签失败");
        }

        return bizRQ;
    }
}
